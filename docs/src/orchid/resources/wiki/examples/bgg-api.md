---
extraJs:
  - 'assets/example/distributions/examples.js'
---

# {{ page.title }}

This example shows how to make and cache API calls with {{ 'Ballast Repository' | anchor }}. It demonstrates fetching
from the [BoardGameGeek][1] [XML API v2][2]** and caching the response in-memory in the `BallastRepository`. When fetching
a HotList, the cached response will be returned, unless "Force Refresh" is checked or the selected hotlist type has 
changed.

How to use:

- Select a "HotList Type" from the dropdown menu
- Hit "Fetch HotList" to request the API response from the Repository, which will determine whether to actually hit the 
  API or just return the cached value.
- You can force the API to called again by having "Force Refresh" checked when you hit "Fetch HotList". Alternatively, 
  if you fetched data from one hotlist type (say Board Games), then change to another type (like Video Games), then the
  list will also be refreshed, even if "Force Refresh" is not checked.

<div id="example_bgg"></div>
<br><br>

_Pro Tip: Open your {{ 'Ballast Debugger' | anchor }} with this page open to see all Ballast activity in real-time, or
just read the browser's Console logs._

** _Disclaimer: Because of CORS restrictions it is not possible to hit the BGG API directly, so the responses have been
cached in this documentation site's domain. This site is not updated on any regular schedule so the data will definitely
out-out-date. These cached responses are for demonstration purposes ONLY, and BGG will always remain the full owner of
the API responses and all data/images within it._

[1]: https://boardgamegeek.com/
[2]: https://boardgamegeek.com/wiki/page/BGG_XML_API2
