Meta:
@category smoke

Scenario: [S-1] Make sure that all services are visible on the start page
Given I am on YANDEX_START page
Then SERVICES_PANEL -> Q_BUTTON element is visible on the page
Then SERVICES_PANEL -> MARKET_BUTTON element is visible on the page
Then SERVICES_PANEL -> VIDEO_BUTTON element is visible on the page
Then SERVICES_PANEL -> IMAGES_BUTTON element is visible on the page
Then SERVICES_PANEL -> NEWS_BUTTON element is visible on the page
Then SERVICES_PANEL -> MAPS_BUTTON element is visible on the page
Then SERVICES_PANEL -> TRANSLATE_BUTTON element is visible on the page
Then SERVICES_PANEL -> MORE_BUTTON element is visible on the page

Scenario: [S-2] Make sure that search page is opened after searching
Given I am on YANDEX_START page
When I type 'automation' into SEARCH_PANEL -> SEARCH_FIELD element without clearing
When I click on SEARCH_PANEL -> SEARCH_BUTTON element
Then SEARCH page is opened