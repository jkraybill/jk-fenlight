#!/usr/bin/env python3
"""Does trakt_indicators_* actually collect the whole account?

Loads the REAL trakt_api.py - the working tree copy, and the pre-patch copy read
straight out of git - against a fake Trakt that behaves the way the live API has
behaved since the 2025 overhaul:

  1. watched endpoints hard-paginate at 100 items per page and ignore `limit`
  2. the `seasons` array is only returned for extended=progress

The Kodi runtime is stubbed, so this tests the collection logic and nothing else.
It does not talk to Trakt and it does not prove the live API's shape - that came
from S97's on-device run (9103 episodes / 1144 shows / 5114 movies).

The RED control is the point. Against a 250-show / 250-movie account, f22a07e:

    episodes=0   movies=100   show_api_calls=1   extended=full

and it WRITES that empty episode list, because set_bulk_tvshow_watched deletes
the whole db_type before inserting. So the pre-patch code does not merely fail to
fill the indicator table, it empties it on the first sync.

    python3 tests/test_trakt_indicators.py

Written workshop S155 (2026-08-11).
"""
import subprocess
import sys, types, importlib.util
from datetime import datetime

RED_COMMIT = 'f22a07e'   # last commit before the S155 pagination fix
ADDON_REL = 'plugin.video.fenlight/resources/lib/apis/trakt_api.py'

TOTAL_SHOWS = 250          # 3 pages at 100/page
TOTAL_MOVIES = 250
SEASONS_PER_SHOW, EPS_PER_SEASON = 2, 5
EXPECTED_EPISODES = TOTAL_SHOWS * SEASONS_PER_SHOW * EPS_PER_SEASON   # 2500
WATCHED_AT = '2026-07-01T12:00:00.000Z'


# ---------------------------------------------------------------- fake Trakt
class FakeTrakt:
    def __init__(self, fail_on_page=None):
        self.fail_on_page = fail_on_page
        self.calls = []

    def _page(self, items, page_no, limit):
        start = (page_no - 1) * limit
        return items[start:start + limit]

    def __call__(self, path, params=None, data=None, is_delete=False,
                 with_auth=True, method=None, pagination=False, page_no=1):
        params = dict(params or {})
        # the old code smuggles extended= into the path; the new code passes it
        # as a parameter. Read both so one fake serves both callers.
        extended = params.get('extended')
        if 'extended=' in path:
            extended = path.split('extended=')[1].split('?')[0].split('&')[0]
        limit = 100                      # Trakt's real cap, whatever was asked for
        self.calls.append({'path': path.split('?')[0], 'extended': extended,
                           'pagination': pagination, 'page_no': page_no})

        if 'watched/shows' in path:
            items = [self._show(i, extended) for i in range(TOTAL_SHOWS)]
        elif 'watched/movies' in path:
            items = [self._movie(i) for i in range(TOTAL_MOVIES)]
        else:
            raise AssertionError('unexpected path %s' % path)

        page_count = (len(items) + limit - 1) // limit
        if pagination:
            if self.fail_on_page and page_no == self.fail_on_page:
                raise RuntimeError('simulated HTTP 500 on page %s' % page_no)
            return (self._page(items, page_no, limit), str(page_count))
        # unpaginated caller: Trakt still only gives it the first page
        return self._page(items, 1, limit)

    def _show(self, i, extended):
        item = {'show': {'title': 'Show %s' % i,
                         'ids': {'tmdb': 1000 + i, 'imdb': None, 'tvdb': None}}}
        if extended == 'progress':
            item['seasons'] = [
                {'number': s, 'episodes': [{'number': e, 'last_watched_at': WATCHED_AT}
                                           for e in range(1, EPS_PER_SEASON + 1)]}
                for s in range(1, SEASONS_PER_SHOW + 1)]
        return item

    def _movie(self, i):
        return {'movie': {'title': 'Movie %s' % i,
                          'ids': {'tmdb': 2000 + i, 'imdb': None, 'tvdb': None}},
                'last_watched_at': WATCHED_AT}


# ------------------------------------------------------------ Kodi stub-out
class Recorder:
    def __init__(self):
        self.tv = None
        self.movies = None
    def set_bulk_tvshow_watched(self, rows): self.tv = list(rows)
    def set_bulk_movie_watched(self, rows): self.movies = list(rows)
    def watched_count(self, db_type): return 0
    def __getattr__(self, name): return lambda *a, **k: None


def _mod(name, **attrs):
    m = types.ModuleType(name)
    for k, v in attrs.items(): setattr(m, k, v)
    return m


def _serial_thread_list(target, items):
    """make_thread_list, run inline.

    Each item is isolated in try/except because that is what a real Thread does:
    an exception kills that thread and the caller never hears about it. Losing
    that isolation would turn the old code's silent data loss into a loud crash
    and flatter it.
    """
    for item in items:
        try: target(item)
        except Exception: pass
    return []


def load(path, recorder):
    stub = lambda *a, **k: None
    # call_trakt is replaced wholesale, so requests is never actually used - but
    # the module imports it at the top and this box has no requests in system python.
    sys.modules.setdefault('requests', _mod('requests', get=stub, post=stub, delete=stub))
    caches = types.ModuleType('caches'); caches.__path__ = []
    modules = types.ModuleType('modules'); modules.__path__ = []
    sys.modules['caches'] = caches
    sys.modules['modules'] = modules
    sys.modules['caches.trakt_cache'] = _mod(
        'caches.trakt_cache', trakt_watched_cache=recorder, clear_all_trakt_cache_data=stub,
        cache_trakt_object=stub, clear_trakt_calendar=stub, reset_activity=stub,
        clear_trakt_list_contents_data=stub, clear_daily_cache=stub,
        clear_trakt_collection_watchlist_data=stub, clear_trakt_hidden_data=stub,
        clear_trakt_recommendations=stub, clear_trakt_list_data=stub, clear_trakt_favorites=stub)
    sys.modules['caches.settings_cache'] = _mod('caches.settings_cache',
                                                get_setting=lambda *a, **k: '0', set_setting=stub)
    sys.modules['caches.main_cache'] = _mod('caches.main_cache', cache_object=stub)
    sys.modules['caches.lists_cache'] = _mod('caches.lists_cache', lists_cache_object=stub)
    sys.modules['modules.kodi_utils'] = _mod(
        'modules.kodi_utils', sleep=stub, with_media_removals=(), get_property=stub,
        logger=lambda *a, **k: None, notification=stub, xbmc_player=stub, confirm_dialog=stub,
        kodi_dialog=stub, addon_installed=stub, addon_enabled=stub, addon=stub, path_check=stub,
        get_icon=stub, clear_property=stub, remove_keys=stub, execute_builtin=stub,
        select_dialog=stub, kodi_refresh=stub, progress_dialog=stub, external=False)
    sys.modules['modules.settings'] = _mod(
        'modules.settings', trakt_user_active=True, show_unaired_watchlist=False,
        lists_sort_order=stub, trakt_client=lambda: 'fake-client-id',
        trakt_secret=lambda: 'fake-secret', tmdb_api_key=lambda: 'fake-tmdb-key')
    sys.modules['modules.metadata'] = _mod('modules.metadata',
                                           movie_meta_external_id=stub, tvshow_meta_external_id=stub)
    sys.modules['modules.utils'] = _mod(
        'modules.utils', sort_list=stub, sort_for_article=stub, make_thread_list=_serial_thread_list,
        get_datetime=datetime.now, timedelta=__import__('datetime').timedelta,
        replace_html_codes=lambda x: x, copy2clip=stub, title_key=stub,
        jsondate_to_datetime=lambda s, f: datetime.strptime(s, f))

    for junk in [k for k in sys.modules if k.startswith('trakt_api_')]: del sys.modules[junk]
    name = path.rsplit('/', 1)[-1][:-3]
    spec = importlib.util.spec_from_file_location(name, path)
    mod = importlib.util.module_from_spec(spec)
    sys.modules[name] = mod
    spec.loader.exec_module(mod)
    return mod


# --------------------------------------------------------------------- runs
def run(path, label, fail_on_page=None):
    rec = Recorder()
    mod = load(path, rec)
    fake = FakeTrakt(fail_on_page=fail_on_page)
    mod.call_trakt = fake
    mod.trakt_indicators_tv()
    mod.trakt_indicators_movies()
    show_calls = [c for c in fake.calls if 'shows' in c['path']]
    return {
        'label': label,
        'episode_rows': None if rec.tv is None else len(rec.tv),
        'movie_rows': None if rec.movies is None else len(rec.movies),
        'show_api_calls': len(show_calls),
        'extended': show_calls[0]['extended'] if show_calls else None,
        'wrote_tv': rec.tv is not None,
        'wrote_movies': rec.movies is not None,
    }


def show(r):
    print('  %-22s episodes=%-6s movies=%-6s show_api_calls=%-3s extended=%s'
          % (r['label'], r['episode_rows'], r['movie_rows'], r['show_api_calls'], r['extended']))


REPO = __file__.rsplit('/', 2)[0]
TMP_RED = '%s/tests/.red_trakt_api.py' % REPO
with open(TMP_RED, 'wb') as f:
    f.write(subprocess.check_output(['git-gordo', '-C', REPO, 'show',
                                     '%s:%s' % (RED_COMMIT, ADDON_REL)]))

print('Account under test: %s shows / %s movies / %s watched episodes\n'
      % (TOTAL_SHOWS, TOTAL_MOVIES, EXPECTED_EPISODES))

print('RED — code at %s (what shipped in the 3.0.0-jk1 zip):' % RED_COMMIT)
old = run(TMP_RED, RED_COMMIT)
show(old)

print('\nGREEN — working tree:')
new = run('%s/%s' % (REPO, ADDON_REL), 'patched')
show(new)

print('\nFAILURE — working tree, Trakt 500s on page 2 of 3:')
broke = run('%s/%s' % (REPO, ADDON_REL), 'patched/page-2-fails', fail_on_page=2)
show(broke)

fails = []
def check(cond, msg):
    print(('  PASS  ' if cond else '  FAIL  ') + msg)
    if not cond: fails.append(msg)

print('\nAssertions:')
check(old['episode_rows'] == 0,
      'RED: %s collects no episodes at all (got %s) — extended=full returns no seasons'
      % (RED_COMMIT, old['episode_rows']))
check(old['wrote_tv'] is True,
      'RED: %s WRITES that empty list (wrote_tv=%s) — set_bulk deletes first, so it wipes'
      % (RED_COMMIT, old['wrote_tv']))
check(old['movie_rows'] == 100,
      'RED: %s collects 100 of %s movies (got %s) — one unpaginated call'
      % (RED_COMMIT, TOTAL_MOVIES, old['movie_rows']))
check(old['show_api_calls'] == 1,
      'RED: %s makes 1 show request (got %s)' % (RED_COMMIT, old['show_api_calls']))
check(new['extended'] == 'progress',
      'GREEN: patched asks for extended=progress (got %s)' % new['extended'])
check(new['show_api_calls'] == 3,
      'GREEN: patched makes 3 show requests, one per page (got %s)' % new['show_api_calls'])
check(new['episode_rows'] == EXPECTED_EPISODES,
      'GREEN: patched collects all %s episodes (got %s)' % (EXPECTED_EPISODES, new['episode_rows']))
check(new['movie_rows'] == TOTAL_MOVIES,
      'GREEN: patched collects all %s movies (got %s)' % (TOTAL_MOVIES, new['movie_rows']))
check(broke['wrote_tv'] is False and broke['wrote_movies'] is False,
      'FAILURE: a failed page writes NOTHING (tv=%s movies=%s) — set_bulk_* deletes before insert, '
      'so a partial write would wipe the rest' % (broke['wrote_tv'], broke['wrote_movies']))

print('\n%s' % ('ALL PASS' if not fails else '%s FAILED' % len(fails)))
sys.exit(1 if fails else 0)
