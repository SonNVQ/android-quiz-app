# Android Quiz App - Visual Package Diagram

## ASCII Package Structure Diagram with Relationships

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ANDROID QUIZ APP ARCHITECTURE                        │
│                                  app.quiz                                      │
└─────────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
                              ┌─────────────────┐
                              │  MainActivity   │
                              │     (Entry)     │
                              └─────────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    ▼                  ▼                  ▼
        ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
        │   DATA LAYER    │  │   UI LAYER      │  │  UTILS LAYER    │
        │   app.quiz.data │  │   app.quiz.ui   │  │  app.quiz.utils │
        └─────────────────┘  └─────────────────┘  └─────────────────┘
                │                      │                      │
                ▼                      ▼                      ▼
    ┌───────────────────────┐ ┌───────────────────────┐ ┌─────────────┐
    │       MODELS          │ │     ACTIVITIES        │ │ SessionMgr  │
    │ ┌─────────────────┐   │ │ ┌─────────────────┐   │ │             │
    │ │ • Flashcard     │◀──┼─┼─│ • CreateFlash   │   │ │ • Auth      │
    │ │ • FlashcardGrp  │◀──┼─┼─│ • FlashcardDet  │   │ │ • Session   │
    │ │ • User          │◀──┼─┼─│ • FlashcardList │   │ │ • Token     │
    │ │ • LoginReq      │◀──┼─┼─│ • Login         │───┼─┼▶│             │
    │ │ • LoginResp     │──▶┼─┼─│ • MyFlashcards  │   │ │             │
    │ │ • SignupReq     │◀──┼─┼─│ • Profile       │───┼─┼▶│             │
    │ │ • PagedResp     │──▶┼─┼─│ • Signup        │   │ │             │
    │ └─────────────────┘   │ │ └─────────────────┘   │ └─────────────┘
    │           ▲           │ │           │           │        ▲
    │           │           │ │           ▼           │        │
    │       REMOTE          │ │      ADAPTERS         │        │
    │ ┌─────────────────┐   │ │ ┌─────────────────┐   │        │
    │ │ • ApiClient     │───┼─┼▶│ • CreateFlash   │   │        │
    │ │ • FlashcardSvc  │───┼─┼▶│ • FlashcardGrp  │   │        │
    │ │                 │   │ │ │ • FlashcardList │   │        │
    │ │ • HTTP Calls    │   │ │ │ • FlashcardSldr │   │        │
    │ │ • JSON Parse    │   │ │ │                 │   │        │
    │ │ • Error Handle  │   │ │ │ • RecyclerView  │   │        │
    │ └─────────────────┘   │ │ │ • ViewHolders   │   │        │
    │           ▲           │ │ └─────────────────┘   │        │
    │           └───────────┼─┼───────────────────────┼────────┘
    └───────────────────────┘ │                       │
                              └───────────────────────┘

    RELATIONSHIP LEGEND:
    ───▶  Uses/Depends on
    ◀───  Provides data to
    ◀──▶  Bidirectional communication

```

## Sub-Package Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         SUB-PACKAGE RELATIONSHIPS                              │
└─────────────────────────────────────────────────────────────────────────────────┘

    app.quiz.ui.activities ◀──────────────────────┐
           │                                      │
           │ [1] Uses models                      │ [8] Session validation
           ▼                                      │
    app.quiz.data.models ──────────────────────┐  │
           │                                   │  │
           │ [2] Serialized by                 │  │
           ▼                                   │  │
    app.quiz.data.remote ──────────────────────┼──┼──▶ app.quiz.utils
           │                                   │  │        │
           │ [3] HTTP requests                 │  │        │ [9] Token injection
           ▼                                   │  │        ▼
    [External API Server] ◀─────────────────────┘  │   SessionManager
           │                                      │        │
           │ [4] JSON responses                    │        │ [10] Auth state
           ▼                                      │        ▼
    app.quiz.data.models ◀─────────────────────────┘   All Activities
           │
           │ [5] Parsed data
           ▼
    app.quiz.ui.adapters
           │
           │ [6] Display data
           ▼
    app.quiz.ui.activities
           │
           │ [7] User interactions
           └──────────────────────────────────────────────┘

    DETAILED RELATIONSHIPS:
    
    ┌─ app.quiz.ui.activities ─┐     ┌─ app.quiz.data.models ─┐
    │                          │ [A] │                        │
    │ • LoginActivity          │────▶│ • LoginRequest         │
    │ • SignupActivity         │     │ • SignupRequest        │
    │ • CreateFlashcardAct     │     │ • Flashcard            │
    │ • MyFlashcardsActivity   │     │ • FlashcardGroup       │
    │ • FlashcardDetailAct     │     │ • User                 │
    │ • FlashcardListActivity  │     │ • PagedResponse        │
    │ • ProfileActivity        │     │ • LoginResponse        │
    └──────────────────────────┘     └────────────────────────┘
                 │                                   │
                 │ [B] Uses adapters                 │ [C] Used by remote
                 ▼                                   ▼
    ┌─ app.quiz.ui.adapters ───┐     ┌─ app.quiz.data.remote ─┐
    │                          │     │                        │
    │ • CreateFlashcardAdapter │     │ • ApiClient            │
    │ • FlashcardGroupAdapter  │     │ • FlashcardService     │
    │ • FlashcardListAdapter   │     │                        │
    │ • FlashcardSliderAdapter │     │ • HTTP operations      │
    │                          │     │ • JSON parsing         │
    └──────────────────────────┘     │ • Error handling       │
                 ▲                   └────────────────────────┘
                 │                                   │
                 │ [D] Data binding                  │ [E] Auth headers
                 │                                   ▼
                 └─────────────────┐     ┌─ app.quiz.utils ────┐
                                   │     │                     │
                                   │     │ • SessionManager    │
                                   │     │   ├─ login()        │
                                   │     │   ├─ logout()       │
                                   │     │   ├─ getToken()     │
                                   │     │   ├─ isLoggedIn()   │
                                   │     │   └─ getUser()      │
                                   │     └─────────────────────┘
                                   │                   │
                                   │ [F] Session data  │ [G] Auth state
                                   └───────────────────┘

```

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           COMPONENT INTERACTIONS                               │
└─────────────────────────────────────────────────────────────────────────────────┘

    USER INTERACTION FLOW:
    
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │   Login     │───▶│    Main     │───▶│MyFlashcards │───▶│CreateFlash  │
    │  Activity   │    │  Activity   │    │  Activity   │    │  Activity   │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
           │                   │                   │                   │
           ▼                   ▼                   ▼                   ▼
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │SessionMgr   │    │SessionMgr   │    │FlashcardGrp │    │CreateFlash  │
    │.login()     │    │.isLoggedIn()│    │Adapter      │    │Adapter      │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
           │                                       │                   │
           ▼                                       ▼                   ▼
    ┌─────────────┐                        ┌─────────────┐    ┌─────────────┐
    │ ApiClient   │                        │FlashcardSvc │    │FlashcardSvc │
    │.authenticate│                        │.getGroups() │    │.createGroup │
    └─────────────┘                        └─────────────┘    └─────────────┘
           │                                       │                   │
           ▼                                       ▼                   ▼
    ┌─────────────┐                        ┌─────────────┐    ┌─────────────┐
    │   Server    │                        │   Server    │    │   Server    │
    │/api/login   │                        │/api/groups  │    │/api/groups  │
    └─────────────┘                        └─────────────┘    └─────────────┘

```

## Architecture Layers Breakdown

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              LAYER DETAILS                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

╔═══════════════════════════════════════════════════════════════════════════════╗
║                                UI LAYER                                      ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║  ACTIVITIES (7 classes)                                                      ║
║  ┌─────────────────────────────────────────────────────────────────────────┐ ║
║  │ • CreateFlashcardActivity.java  - Create new flashcard groups           │ ║
║  │ • FlashcardDetailActivity.java  - View individual flashcard details     │ ║
║  │ • FlashcardListActivity.java    - Browse available flashcard groups     │ ║
║  │ • LoginActivity.java            - User authentication                    │ ║
║  │ • MyFlashcardsActivity.java     - User's personal flashcard collection  │ ║
║  │ • ProfileActivity.java          - User profile management               │ ║
║  │ • SignupActivity.java           - New user registration                 │ ║
║  └─────────────────────────────────────────────────────────────────────────┘ ║
║                                                                               ║
║  ADAPTERS (4 classes)                                                        ║
║  ┌─────────────────────────────────────────────────────────────────────────┐ ║
║  │ • CreateFlashcardAdapter.java   - Manage flashcard creation interface   │ ║
║  │ • FlashcardGroupAdapter.java    - Display flashcard groups in lists     │ ║
║  │ • FlashcardListAdapter.java     - Handle flashcard list presentations   │ ║
║  │ • FlashcardSliderAdapter.java   - Manage flashcard slider/carousel      │ ║
║  └─────────────────────────────────────────────────────────────────────────┘ ║
╚═══════════════════════════════════════════════════════════════════════════════╝

╔═══════════════════════════════════════════════════════════════════════════════╗
║                               DATA LAYER                                     ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║  MODELS (7 classes)                                                          ║
║  ┌─────────────────────────────────────────────────────────────────────────┐ ║
║  │ • Flashcard.java        - Individual flashcard entity                   │ ║
║  │ • FlashcardGroup.java   - Collection of related flashcards              │ ║
║  │ • LoginRequest.java     - Login API request structure                   │ ║
║  │ • LoginResponse.java    - Login API response structure                  │ ║
║  │ • PagedResponse.java    - Generic paginated response wrapper            │ ║
║  │ • SignupRequest.java    - Registration API request structure            │ ║
║  │ • User.java             - User entity and profile information           │ ║
║  └─────────────────────────────────────────────────────────────────────────┘ ║
║                                                                               ║
║  REMOTE (2 classes)                                                          ║
║  ┌─────────────────────────────────────────────────────────────────────────┐ ║
║  │ • ApiClient.java        - HTTP client configuration and setup           │ ║
║  │ • FlashcardService.java - API service for flashcard operations          │ ║
║  └─────────────────────────────────────────────────────────────────────────┘ ║
╚═══════════════════════════════════════════════════════════════════════════════╝

╔═══════════════════════════════════════════════════════════════════════════════╗
║                              UTILS LAYER                                     ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║  UTILITIES (1 class)                                                         ║
║  ┌─────────────────────────────────────────────────────────────────────────┐ ║
║  │ • SessionManager.java   - User session and authentication management    │ ║
║  │   ├─ Token storage and retrieval                                        │ ║
║  │   ├─ Login state management                                             │ ║
║  │   ├─ User session persistence                                           │ ║
║  │   └─ Authentication header injection                                    │ ║
║  └─────────────────────────────────────────────────────────────────────────┘ ║
╚═══════════════════════════════════════════════════════════════════════════════╝

```

## Data Flow Visualization

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                DATA FLOW                                       │
└─────────────────────────────────────────────────────────────────────────────────┘

    USER ACTION → UI → DATA → NETWORK → SERVER
    
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │    User     │───▶│  Activity   │───▶│   Model     │───▶│   Server    │
    │   Clicks    │    │  Handles    │    │  Processes  │    │  Responds   │
    │   Button    │    │   Event     │    │    Data     │    │    with     │
    └─────────────┘    └─────────────┘    └─────────────┘    │    JSON     │
           ▲                   ▲                   ▲         └─────────────┘
           │                   │                   │                   │
           │                   │                   │                   ▼
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │    UI       │◀───│  Adapter    │◀───│ FlashcardSvc│◀───│  ApiClient  │
    │  Updates    │    │  Notifies   │    │   Parses    │    │   Receives  │
    │  Display    │    │   Changes   │    │  Response   │    │  Response   │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

    AUTHENTICATION FLOW:
    
    Login → SessionManager → ApiClient → Server → Token → SessionManager → Activities
    
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │LoginActivity│───▶│SessionMgr   │───▶│ ApiClient   │───▶│   Server    │
    │.login()     │    │.authenticate│    │.post()      │    │/api/login   │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
           ▲                   ▲                   ▲                   │
           │                   │                   │                   ▼
    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
    │Navigate to  │◀───│Store Token  │◀───│Parse JWT    │◀───│Return Token │
    │MainActivity │    │& User Info  │    │Response     │    │& User Data  │
    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

```

## Sub-Package Relationship Matrix

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        RELATIONSHIP MATRIX                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

    FROM \ TO          │ activities │ adapters │ models │ remote │ utils │
    ───────────────────┼────────────┼──────────┼────────┼────────┼───────┤
    ui.activities      │     ◯      │    ▶     │   ▶    │   ◯    │   ▶   │
    ui.adapters        │     ◀      │    ◯     │   ▶    │   ◯    │   ◯   │
    data.models        │     ◀      │    ◀     │   ◯    │   ◀▶   │   ◯   │
    data.remote        │     ◯      │    ◯     │   ▶    │   ◯    │   ▶   │
    utils              │     ◀      │    ◯     │   ◯    │   ◀    │   ◯   │
    
    SYMBOLS:
    ▶  = Uses/Depends on (outgoing dependency)
    ◀  = Provides data to (incoming dependency)
    ◀▶ = Bidirectional relationship
    ◯  = No direct relationship
    
    SPECIFIC RELATIONSHIPS:
    
    1. ui.activities ▶ ui.adapters
       - Activities instantiate and configure adapters
       - Pass data and click listeners to adapters
       
    2. ui.activities ▶ data.models
       - Activities use model classes for data representation
       - Create instances of LoginRequest, SignupRequest, etc.
       
    3. ui.activities ▶ utils
       - Activities call SessionManager for authentication
       - Check login state and manage user sessions
       
    4. ui.adapters ▶ data.models
       - Adapters bind model data to views
       - Display Flashcard, FlashcardGroup properties
       
    5. data.remote ▶ data.models
       - Remote services serialize/deserialize models
       - Convert JSON responses to model objects
       
    6. data.remote ▶ utils
       - Remote services get auth tokens from SessionManager
       - Include authentication headers in API calls
       
    7. data.models ◀▶ data.remote
       - Models are serialized for API requests
       - Models are created from API responses

```

## Complete Dependency Graph

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           COMPLETE DEPENDENCY GRAPH                            │
└─────────────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────────────┐
                    │          app.quiz.utils             │
                    │        SessionManager.java          │
                    │  ┌─────────────────────────────────┐ │
                    │  │ • login(credentials)            │ │
                    │  │ • logout()                      │ │
                    │  │ • getToken() → String           │ │
                    │  │ • isLoggedIn() → boolean        │ │
                    │  │ • getUser() → User              │ │
                    │  └─────────────────────────────────┘ │
                    └─────────────────────────────────────┘
                              ▲                 │
                              │                 │
                    ┌─────────┴─────────┐      │
                    │                   │      │
                    │ [Auth Headers]    │      │ [Session State]
                    │                   │      │
                    ▼                   │      ▼
    ┌─────────────────────────────────────┐    │    ┌─────────────────────────────────────┐
    │       app.quiz.data.remote          │    │    │        app.quiz.ui.activities       │
    │                                     │    │    │                                     │
    │  ┌─────────────────────────────────┐│    │    │ ┌─────────────────────────────────┐ │
    │  │ ApiClient.java                  ││    │    │ │ • LoginActivity.java            │ │
    │  │ • setupHttpClient()             ││    │    │ │ • SignupActivity.java           │ │
    │  │ • addAuthHeaders()              ││    │    │ │ • MyFlashcardsActivity.java     │ │
    │  │ • handleErrors()                ││    │    │ │ • CreateFlashcardActivity.java  │ │
    │  └─────────────────────────────────┘│    │    │ │ • FlashcardDetailActivity.java  │ │
    │                                     │    │    │ │ • FlashcardListActivity.java    │ │
    │  ┌─────────────────────────────────┐│    │    │ │ • ProfileActivity.java          │ │
    │  │ FlashcardService.java           ││    │    │ └─────────────────────────────────┘ │
    │  │ • getFlashcardGroups()          ││    │    └─────────────────────────────────────┘
    │  │ • createFlashcardGroup()        ││    │                      │
    │  │ • updateFlashcard()             ││    │                      │ [Uses Adapters]
    │  │ • deleteFlashcard()             ││    │                      ▼
    │  └─────────────────────────────────┘│    │    ┌─────────────────────────────────────┐
    └─────────────────────────────────────┘    │    │        app.quiz.ui.adapters         │
                    │                          │    │                                     │
                    │ [JSON Serialization]     │    │ ┌─────────────────────────────────┐ │
                    ▼                          │    │ │ • CreateFlashcardAdapter.java   │ │
    ┌─────────────────────────────────────┐    │    │ │ • FlashcardGroupAdapter.java    │ │
    │       app.quiz.data.models          │    │    │ │ • FlashcardListAdapter.java     │ │
    │                                     │    │    │ │ • FlashcardSliderAdapter.java   │ │
    │ ┌─────────────────────────────────┐ │    │    │ └─────────────────────────────────┘ │
    │ │ • Flashcard.java                │ │    │    └─────────────────────────────────────┘
    │ │ • FlashcardGroup.java           │ │    │                      ▲
    │ │ • User.java                     │ │    │                      │
    │ │ • LoginRequest.java             │ │    │                      │ [Data Binding]
    │ │ • LoginResponse.java            │ │    │                      │
    │ │ • SignupRequest.java            │ │    └──────────────────────┘
    │ │ • PagedResponse.java            │ │
    │ └─────────────────────────────────┘ │
    └─────────────────────────────────────┘

```

## Legend

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  LEGEND                                        │
└─────────────────────────────────────────────────────────────────────────────────┘

    COMPONENT TYPES:
    ┌─────────────┐     Activity/Fragment Class
    │             │
    └─────────────┘
    
    ┌─────────────┐     Data Model Class
    │             │
    └─────────────┘
    
    ┌─────────────┐     Service/Utility Class
    │             │
    └─────────────┘
    
    RELATIONSHIP ARROWS:
    ───▶             Uses/Depends on (Composition/Aggregation)
    ◀───             Provides data to (Data Flow)
    ◀──▶             Bidirectional communication
    ┌───┐
    │   │             Inheritance/Implementation
    └───┘
    
    PACKAGE BOUNDARIES:
    ╔═══════════╗     Package/Layer Boundary
    ║           ║
    ╚═══════════╝
    
    ANNOTATIONS:
    • Bullet Point   Class Method/Feature
    [Label]          Relationship Type/External System
    
    RELATIONSHIP TYPES:
    [Uses Models]        - Activities instantiate and manipulate model objects
    [Data Binding]       - Adapters bind model data to UI components
    [JSON Serialization] - Remote services convert models to/from JSON
    [Auth Headers]       - Utils provide authentication tokens to remote
    [Session State]      - Utils manage user session across activities

```