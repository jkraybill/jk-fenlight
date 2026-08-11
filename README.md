> **This fork (jk-fenlight):** JK's deployment copy, at **2.2.99**. The version is deliberately parked at the top of the 2.2.x range so a stray upstream 2.2.x cannot sort above it and overwrite the local fixes.
>
> Local fixes: the Trakt watched endpoints are paginated and TV asks for `extended=progress` (S155). Before that fix the addon collected 0 episodes and the first 100 movies of any account, and *wrote* the empty episode list — `set_bulk_tvshow_watched` deletes the whole `db_type` before inserting, so it emptied the indicator table rather than just failing to fill it. Root-caused S97 against the live API, fixed in-repo S155; `tests/test_trakt_indicators.py` holds the RED/GREEN control.
>
> The README below is inherited from thejason40's FenLight+; its install instructions and version numbers describe *his* repo, not this fork. In particular, do not point "Manage Addon Updates" at the upstream repo — an update from it overwrites these fixes.

I'm jokingly called this FenLight+. There was always 1 thing I wished Fen could do that it couldn't and that was delete RD Cloud files from the results screen so that I didn't have to go into My Services everytime I accidently added a bad package to the cloud. Now it can. 

Since then I've also added more features, such as:
 - an option to unmark previous episode as watched in Next Episodes list
 - re-enabled 'Rollback to Previous Version' option that Tikipeter included in older versions of FenLight
 - UK specific options for "Trending" and "Providers" lists
 - TMDB Lists integration and abilty to export Trakt lists to TMDB

I've also released a companion android app because I got fed up with searching for movies using a TV Remote. You can download it here: `https://thejason40.github.io/apk/`

This is the first Kodi addon I've ever edited so Tikipeter would probably shed a tear at how I butchered his code. I might keep working to improve this, I might not.

<h2>To Update From FenLight</h2>

In Kodi, go to Addons, open Fen Light -> Tools -> Settings -> General -> scroll down to 'Manage Addon Updates' and change these two fields:

<b>Github Username:</b> thejason40<br />
<b>Github Repo Address:</b> thejason40.github.io

Now go back to the Tools menu, and select Update Utilities -> Check For Updates

<h2>To Install Fresh</h2>

Go to Kodi -> File Manager -> Add Source -> add

`https://thejason40.github.io/packages`

call it "FenPlus"

Then go to Kodi -> Settings -> Add Ons -> Install from Zip -> FenPlus -> Install FenLight+ 2.0.10.

<hr>
<i>Full credit to Tikipeter for creating Fen and FenLight. Thanks to minicoz for forking Tiki's repository before it was taken down.</i>

---

*A [Project Gordo](https://github.com/jkraybill/project-gordo) umbrella project, managed via [jk-gordo-workshop](https://github.com/jkraybill/jk-gordo-workshop).*
