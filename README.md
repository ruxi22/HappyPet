# HappyPet

HappyPet is an Android application designed to help pet owners manage their pet care routines efficiently. From feeding schedules to vet visits, HappyPet ensures that your furry, feathery, or scaly friends are always taken care of. The app allows users to create profiles for their pets, customize their pet types, and receive reminders for pet care activities.

---

## Features

1. **User Authentication**:
   - Login and Sign-Up functionalities to manage user-specific pet data.

2. **Pet Profile Management**:
   - Add detailed profiles for your pets, including name, age, breed/species, and type.
   - Custom pet types for unique or uncommon pets.

3. **Animal Selection**:
   - Predefined types like Dog, Cat, Rabbit, Bird, Fish, and Reptile.

4. **Pet Care Reminders**:
   - Notifications to remind users about care routines (e.g., feeding, walking, vet appointments).

5. **Pet Status Page**:
   - View the pet’s profile and essential details in one place.

6. **Customizable Themes**:
   - Support for dynamic light and dark themes for better user experience.

---

## Android Components Used

1. **Foreground Services**:
   - **PetReminderService**: Runs in the foreground to display ongoing pet care reminders through notifications. This ensures that reminders are always visible to the user.
     - **Code Reference**:
       ```kotlin
       val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
           .setContentTitle("Pet Reminder")
           .setContentText("Don't forget to update your pet's profile!")
           .build()
       startForeground(NOTIFICATION_ID, notification)
       ```

2. **Background Services**:
   - **GetFactsAPIService** and similar services handle tasks such as fetching data or scheduling events in the background.
     - **Code Reference**:
       ```kotlin
       override fun doWork(): Result {
           val response = api.getPetFacts()
           saveFactsToDatabase(response)
           return Result.success()
       }
       ```

3. **Bound Services**:
   - **BoundPetService**: Facilitates communication between components for data syncing in real-time.
     - **Code Reference**:
       ```kotlin
       override fun onBind(intent: Intent): IBinder {
           return binder
       }
       ```

4. **Intents**:
   - Used extensively for navigation and inter-component communication.
     - **Code Reference**:
       ```kotlin
       val intent = Intent(this, PetProfileActivity::class.java)
       intent.putExtra("animalType", "dog")
       startActivity(intent)
       ```

5. **Activities**:
   - **LoginActivity**: Manages user login.
     - **Code Reference**:
       ```kotlin
       val user = withContext(Dispatchers.IO) {
           db.userDao().getUserByCredentials(email, password)
       }
       ```
   - **SignUpActivity**: Facilitates new user registration.
     - **Code Reference**:
       ```kotlin
       db.userDao().insert(User(email, username, password))
       ```
   - **MainActivity**: Central hub for pet selection and reminder service initiation.
     - **Code Reference**:
       ```kotlin
       val serviceIntent = Intent(this, PetReminderService::class.java)
       startService(serviceIntent)
       ```
   - **AddCustomPetActivity**: Allows users to add custom pet types.
     - **Code Reference**:
       ```kotlin
       editor.putString("custom_pet_type", petType)
       editor.putString("custom_pet_name", petName)
       ```
   - **PetProfileActivity**: Enables managing detailed pet profiles.
     - **Code Reference**:
       ```kotlin
       val pet = Pet(name, age, breed, animalType)
       db.petDao().insertPet(pet)
       ```
   - **StatusPageActivity**: Displays pet profiles and other user-specific information.
     - **Code Reference**:
       ```kotlin
       val pet = db.petDao().getPetById(user.petId)
       ```

6. **Broadcast Receivers**:
   - **NotificationReceiver**: Listens for broadcast intents to display pet care reminders as notifications.
     - **Code Reference**:
       ```kotlin
       override fun onReceive(context: Context, intent: Intent) {
           val message = intent.getStringExtra("NOTIFICATION_MESSAGE") ?: "Reminder!"
           ```
   - **PetHealthReceiver**: Handles additional broadcasts related to pet health updates.
     - **Code Reference**:
       ```kotlin
       val intentFilter = IntentFilter("com.example.happypet.HEALTH_UPDATE")
       context.registerReceiver(receiver, intentFilter)
       ```

7. **Shared Preferences**:
   - Used to store user-specific preferences, such as logged-in user data and custom pet details.
     - **Code Reference**:
       ```kotlin
       val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
       with(sharedPref.edit()) {
           putString("userEmail", email)
           apply()
       }
       ```

8. **Content Providers**:
   - A custom Content Provider allows the app to share pet data securely with other applications or components.
     - **Code Reference**:
       ```kotlin
       override fun query(uri: Uri, projection: Array<out String>?, ...): Cursor? {
           return db.queryPets()
       }
       ```

9. **Database (Room)**:
   - **AppDatabase** with entities like **User** and **Pet** ensures structured local data storage.
     - **Code Reference**:
       ```kotlin
       @Entity(tableName = "pets")
       data class Pet(
           @PrimaryKey(autoGenerate = true) val id: Int = 0,
           val name: String,
           val age: Int,
           val breed: String,
           val animalType: String
       )
       ```

10. **External APIs**:
    - External data fetching is managed via **RetrofitInstance**, which integrates with third-party APIs to retrieve relevant information for the app.
      - **Code Reference**:
        ```kotlin
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        ```

11. **Notifications**:
    - Notifications are implemented through **PetReminderService** and **NotificationReceiver**, ensuring users are timely reminded of pet care activities.
      - **Code Reference**:
        ```kotlin
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Reminder")
            .setContentText(message)
            .build()
        ```

---

## How It Works

1. **User Authentication**:
   - Users create accounts or log in using their credentials. On successful login, user data is fetched from the Room database.

2. **Pet Profile Creation**:
   - Users can select a predefined animal type or create a custom type for their pet. Detailed profiles include the pet’s name, age, and breed/species.

3. **Notifications**:
   - Once the user adds a pet, reminders are sent periodically via the foreground service to prompt timely care.
   - Notifications can also be triggered through the **NotificationReceiver**, ensuring the user is reminded even through broadcast events.

4. **Dynamic Navigation**:
   - The app dynamically redirects users based on their data. For example, if a logged-in user already has a pet profile, they are redirected to the **StatusPageActivity** instead of the main selection screen.

---

