🐦 The Bird Watcher App

A mobile application for logging and managing bird watching trips and sightings, 
built as part of the COMP1424 Mobile Application Development coursework at the University of Greenwich.
The core app was built in Android Studio using Kotlin. It uses SQLite as the local database, managed through a custom database helper class with two tables — 
one for trips and one for bird sightings, linked by a foreign key so deleting a trip automatically removes its sightings. 
The UI is built with standard Android components: Activities for each screen, RecyclerView with a custom BirdSightingAdapter for displaying cards, 
and native Android date/time picker dialogs to prevent invalid input. Geolocation was added using Android's location permissions API to auto-fill the trip 
location field, with manual entry kept as a fallback. Firebase Firestore was integrated for cloud sync, allowing trips and sightings to be uploaded from 
the app with a single button tap.

📋 Features
#FeatureStatus
1. Enter trip details (name, date, time, location, duration, description)✅
2. Store, view, and delete trip details via SQLite✅
3. Add bird sightings with species selection and custom entry✅
4. Geolocation to auto-fill trip location✅
5. Version control via Azure DevOps✅


🏗️ Architecture
Native Android App (Kotlin)

SQLite database with trips and bird sightings tables
Firebase Firestore for cloud sync
Activities: Welcome, My Trips, Add/Edit Trip, Trip Details, Add Bird Sighting
RecyclerView with card-based adapters for trips and sightings.


📱 App Flow
Creating a Trip & Adding a Bird Sighting.

1. Launch the app → Welcome Page.
<img width="174" height="374" alt="fig01_welcome_page" src="https://github.com/user-attachments/assets/597aaaea-68a3-4fba-ad78-835ef75da6ff" />


2. Tap + on My Trips screen.
<img width="174" height="374" alt="fig03_create_trip" src="https://github.com/user-attachments/assets/20919965-638f-46dd-8341-a1dc818bde10" />


3. Add trip details.
<img width="174" height="374" alt="fig04_add_trip_form" src="https://github.com/user-attachments/assets/29521ac1-fbe8-442e-aecb-3d2454040153" />


4. Add the Trip date through calendar picker
<img width="174" height="374" alt="fig05_calendar_picker" src="https://github.com/user-attachments/assets/06cc94f2-ecee-4f54-9b4f-8df89be02430" />


5. Add the trip time.
<img width="174" height="374" alt="fig06_time_picker" src="https://github.com/user-attachments/assets/be22117c-87c8-468d-9a12-40d1687a6dde" />


6. Enable Geolocation.
<img width="174" height="374" alt="fig07_geolocation" src="https://github.com/user-attachments/assets/85344dc6-ddcb-4b70-864d-ec5ce94ec064" />


7. click "Save" trip. 
<img width="174" height="374" alt="fig08_save_trip" src="https://github.com/user-attachments/assets/4639ce18-0de2-4bae-959d-6b64c92667dd" />


8. View saved Trip
<img width="174" height="374" alt="fig09_trip_saved" src="https://github.com/user-attachments/assets/79c3b755-c6fc-42a8-9b63-429c94c2fbdc" />





							   
Adding a Bird Sighting

1. Tap + Add Bird Sighting.
<img width="174" height="374" alt="fig13_add_bird_sighting" src="https://github.com/user-attachments/assets/5624020f-10ad-474a-9929-2bb44dc9eaf0" />

2. Select species from dropdown (Robin, Eagle, Hawk, Kingfisher, etc.) or choose Custom (Type your own).
<img width="174" height="374" alt="fig14_species_dropdown_left" src="https://github.com/user-attachments/assets/7055785d-67d9-4096-b20b-869d146fe3ea" />
<img width="174" height="374" alt="fig15_species_dropdown_right" src="https://github.com/user-attachments/assets/29536bfc-d2a9-48cd-87f1-570187f09daa" />

4. Enter quantity, spot description, and comments.
<img width="174" height="374" alt="fig16_add_bird_form" src="https://github.com/user-attachments/assets/51c7b4ae-a55c-4766-a5a7-74edf77fb2b4" />

6. Tap Add Bird.
<img width="174" height="374" alt="fig17_bird_added" src="https://github.com/user-attachments/assets/2c301ca2-8241-4671-b306-b3966a6f8d2a" />


Deleting a Trip.

1. Open trip details and Tap Delete Trip.
<img width="174" height="374" alt="fig10_delete_trip_button" src="https://github.com/user-attachments/assets/507ecbe2-0708-477b-904e-7e3e8de4a40f" />

2. Confirm in the dialog — this cannot be undone and removes all linked sightings.
<img width="174" height="374" alt="fig11_delete_confirm" src="https://github.com/user-attachments/assets/598814f4-43fa-46cf-b66e-af2ce1e96e40" />



Syncing into the cloud

1. From My Trips, tap Upload All to Cloud.
<img width="174" height="374" alt="fig19_upload_to_cloud" src="https://github.com/user-attachments/assets/07e96cf2-2235-4623-9db7-250d1fca5005" />

2. Confirmation toast appears on success.
<img width="174" height="374" alt="fig20_trip_uploaded" src="https://github.com/user-attachments/assets/b781fa3d-b3a7-4594-9091-b9ed682456de" />
























