# Yet another shopping list
Boring, right? Right, but I actualy do it for myself. Check out the problem definition. This project is under construction.
## Problem definition
When doing the big weekly shopping, I always go to the same store. I always follow the same path and the items I collect are also similar each time. Still, I'm the kind of guy who forgets stuff so I have to get them written down anyway. I've been using Google Keep notes with checked lists. The problem is that I always order the items when I create the list so they appear in the order I'm going to encounter them on the store's shelf.
## Solution
I wanna the app to take care of the ordering. It should learn from me and take over eventually.
### Smart ordering
The app learns the right order when the user drags and drops the items. At some point, it should know the right place for every item in the store. Well, at least those I'm buying.
### AI grouping
If I enter the shop departments name in the order I'm visiting them, I might be able to use on device AI to put the newly entered items in the right groups, that were ordered by the user. For instance shop layout could be a lust like: "Bakery, Fruits, Vegetables, Meat,.. ".
This gets complicated due to usage of KMP. 

## Tech stack
### Compose
Greatest Android invention since ActionBarSherlock (I guess I'm a boomer).
#### Drag & drop
I found a [great library](https://github.com/Calvin-LL/Reorderable/?tab=readme-ov-file#lazycolumn) that does the job but there are issues which I will address later.
### Coroutines & flows
Used for reactive data models.
### Kotlin multiplatform
So far this is just an Android app but I'm trying to make some layers of the code to be shared with iOS using kotlin multiplatform.
#### Compose resources
I'm using Compose Resources to handle translations (icons later). I already encountered [a problem](https://issuetracker.google.com/issues/348208777) with Compose @Previews. It just doesn't work with it. I had to create a method that would keep previews rendering:
```
@Composable
fun stringResourceCompat(resource: StringResource, previewFallback: String): String {
    return if (LocalInspectionMode.current) {
        // We are in a @Preview composable
        previewFallback
    } else {
        // We are in a real runtime environment
        stringResource(resource)
    }
}
```
### Compose navigation
Compose navigation with bottom navigation bar. Proper compose, not some ulgy port from the early days.
### Room 
Room layer lies in the KMP module. For lists I use flows that take care of the data updates between database and UI.
### AI Design generator
I used [Stitch](https://stitch.withgoogle.com/) to create designs for this app and I'm really happy about it
### Gemini
Not gonna lie, I've generated a good portion of the code which I later groomed to fit the quality I'm used to. I even used `Design png -> Compose UI ` generation from Android Studio Canary. What a time to be alive! I got a lot of help with setting up KMP as well, as this was my first time doing it. I'm collecting meta prompts for code generation inside /gemini folder. These prompts making the code generation more suited to the project I'm working on. 
