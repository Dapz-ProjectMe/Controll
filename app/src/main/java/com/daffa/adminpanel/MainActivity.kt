package com.daffa.adminpanel

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var firestore: FirebaseFirestore

    private lateinit var statusText: TextView

    private lateinit var deviceCountText: TextView

    private lateinit var deviceContainer: LinearLayout

    private lateinit var logoutButton: Button

    private var deviceListener: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()


        statusText = findViewById(R.id.statusText)

        deviceCountText = findViewById(R.id.deviceCountText)

        deviceContainer = findViewById(R.id.deviceContainer)

        logoutButton = findViewById(R.id.logoutButton)


        logoutButton.setOnClickListener {

            auth.signOut()

            showLoginDialog()

        }


        if (auth.currentUser == null) {

            showLoginDialog()

        } else {

            startDeviceListener()

        }

    }


    // =========================================================
    // LOGIN
    // =========================================================

    private fun showLoginDialog() {

        val layout = LinearLayout(this)

        layout.orientation = LinearLayout.VERTICAL

        layout.setPadding(
            50,
            20,
            50,
            10
        )


        val emailInput = android.widget.EditText(this)

        emailInput.hint = "Email"


        val passwordInput = android.widget.EditText(this)

        passwordInput.hint = "Password"

        passwordInput.inputType = 129


        layout.addView(emailInput)

        layout.addView(passwordInput)


        AlertDialog.Builder(this)

            .setTitle("ADMIN LOGIN")

            .setView(layout)

            .setCancelable(false)

            .setPositiveButton("LOGIN", null)

            .setNegativeButton("CANCEL") { _, _ ->

                finish()

            }

            .create()

            .also { dialog ->

                dialog.setOnShowListener {

                    val loginButton =
                        dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                        )


                    loginButton.setOnClickListener {

                        val email =
                            emailInput.text
                                .toString()
                                .trim()


                        val password =
                            passwordInput.text
                                .toString()


                        if (email.isEmpty() ||
                            password.isEmpty()
                        ) {

                            Toast.makeText(
                                this,
                                "Email dan password harus diisi",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@setOnClickListener

                        }


                        loginButton.isEnabled = false


                        auth.signInWithEmailAndPassword(
                            email,
                            password
                        )

                            .addOnSuccessListener {

                                dialog.dismiss()

                                Toast.makeText(
                                    this,
                                    "Login berhasil",
                                    Toast.LENGTH_SHORT
                                ).show()

                                startDeviceListener()

                            }

                            .addOnFailureListener {

                                loginButton.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "Login gagal: ${it.message}",
                                    Toast.LENGTH_LONG
                                ).show()

                            }

                    }

                }

                dialog.show()

            }

    }


    // =========================================================
    // DEVICE LISTENER
    // =========================================================

    private fun startDeviceListener() {

        statusText.text = "Connected to Firebase"


        deviceListener?.remove()


        deviceListener =
            firestore.collection("devices")

                .addSnapshotListener { snapshot, error ->


                    if (error != null) {

                        statusText.text =
                            "Firebase error: ${error.message}"

                        return@addSnapshotListener

                    }


                    deviceContainer.removeAllViews()


                    if (snapshot == null ||
                        snapshot.isEmpty
                    ) {

                        deviceCountText.text =
                            "Devices: 0"


                        val emptyText =
                            TextView(this)


                        emptyText.text =
                            "Belum ada device yang terdaftar."


                        emptyText.textSize = 16f


                        deviceContainer.addView(
                            emptyText
                        )


                        return@addSnapshotListener

                    }


                    deviceCountText.text =
                        "Devices: ${snapshot.size()}"


                    for (document in snapshot.documents) {

                        createDeviceCard(
                            document
                        )

                    }

                }

    }


    // =========================================================
    // DEVICE CARD
    // =========================================================

    private fun createDeviceCard(
        document: com.google.firebase.firestore.DocumentSnapshot
    ) {

        val deviceId = document.id


        val deviceName =
            document.getString("deviceName")
                ?: "Unknown Device"


        val manufacturer =
            document.getString("manufacturer")
                ?: "Unknown"


        val model =
            document.getString("model")
                ?: "Unknown"


        val androidVersion =
            document.getString("androidVersion")
                ?: "Unknown"


        val online =
            document.getBoolean("online")
                ?: false


        val locationSharing =
            document.getBoolean("locationSharing")
                ?: false


        val remoteControl =
            document.getBoolean("remoteControlEnabled")
                ?: false


        val card =
            LinearLayout(this)


        card.orientation =
            LinearLayout.VERTICAL


        card.setPadding(
            25,
            20,
            25,
            20
        )


        val name =
            TextView(this)


        name.text =
            "${if (online) "🟢" else "⚪"} $deviceName"


        name.textSize = 19f

        name.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )


        val modelText =
            TextView(this)


        modelText.text =
            "$manufacturer $model\nAndroid $androidVersion"


        modelText.textSize = 15f


        val locationText =
            TextView(this)


        locationText.text =
            "Location sharing: " +
                    if (locationSharing)
                        "ON"
                    else
                        "OFF"


        val controlText =
            TextView(this)


        controlText.text =
            "Remote control: " +
                    if (remoteControl)
                        "ON"
                    else
                        "OFF"


        val button =
            Button(this)


        button.text =
            "OPEN DEVICE"


        button.setOnClickListener {

            showDeviceDetails(
                deviceId
            )

        }


        card.addView(name)

        card.addView(modelText)

        card.addView(locationText)

        card.addView(controlText)

        card.addView(button)


        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )


        params.setMargins(
            0,
            0,
            0,
            20
        )


        card.layoutParams =
            params


        deviceContainer.addView(card)

    }


    // =========================================================
    // DEVICE DETAILS
    // =========================================================

    private fun showDeviceDetails(
        deviceId: String
    ) {

        firestore.collection("devices")
            .document(deviceId)
            .get()

            .addOnSuccessListener { document ->

                if (!document.exists()) {

                    Toast.makeText(
                        this,
                        "Device tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener

                }


                val deviceName =
                    document.getString("deviceName")
                        ?: "Unknown"


                val manufacturer =
                    document.getString("manufacturer")
                        ?: "Unknown"


                val model =
                    document.getString("model")
                        ?: "Unknown"


                val androidVersion =
                    document.getString("androidVersion")
                        ?: "Unknown"


                val appVersion =
                    document.getString("appVersion")
                        ?: "Unknown"


                val online =
                    document.getBoolean("online")
                        ?: false


                val locationSharing =
                    document.getBoolean(
                        "locationSharing"
                    ) ?: false


                val remoteControl =
                    document.getBoolean(
                        "remoteControlEnabled"
                    ) ?: false


                val permissions =
                    document.get(
                        "permissions"
                    )


                val info = """

                    Device ID:
                    $deviceId

                    Device:
                    $deviceName

                    Manufacturer:
                    $manufacturer

                    Model:
                    $model

                    Android:
                    $androidVersion

                    App Version:
                    $appVersion

                    Online:
                    $online

                    Location Sharing:
                    $locationSharing

                    Remote Control:
                    $remoteControl

                    Permissions:
                    $permissions

                """.trimIndent()


                AlertDialog.Builder(this)

                    .setTitle(deviceName)

                    .setMessage(info)

                    .setPositiveButton(
                        "FLASHLIGHT ON"
                    ) { _, _ ->

                        sendFlashlightCommand(
                            deviceId,
                            true
                        )

                    }

                    .setNegativeButton(
                        "FLASHLIGHT OFF"
                    ) { _, _ ->

                        sendFlashlightCommand(
                            deviceId,
                            false
                        )

                    }

                    .setNeutralButton(
                        "LOCATION"
                    ) { _, _ ->

                        showLocation(
                            deviceId
                        )

                    }

                    .show()

            }

    }


    // =========================================================
    // FLASHLIGHT COMMAND
    // =========================================================

    private fun sendFlashlightCommand(
        deviceId: String,
        enabled: Boolean
    ) {

        val type =
            if (enabled)
                "FLASHLIGHT_ON"
            else
                "FLASHLIGHT_OFF"


        firestore.collection("commands")

            .document(deviceId)

            .collection("items")

            .document()

            .set(
                mapOf(

                    "type" to type,

                    "status" to "pending",

                    "createdAt" to
                            FieldValue.serverTimestamp()

                )
            )

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    if (enabled)
                        "Perintah flashlight ON dikirim"
                    else
                        "Perintah flashlight OFF dikirim",
                    Toast.LENGTH_SHORT
                ).show()

            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Gagal mengirim perintah",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }


    // =========================================================
    // LOCATION
    // =========================================================

    private fun showLocation(
        deviceId: String
    ) {

        firestore.collection("locations")
            .document(deviceId)
            .get()

            .addOnSuccessListener { document ->

                if (!document.exists()) {

                    AlertDialog.Builder(this)

                        .setTitle("LOCATION")

                        .setMessage(
                            "Belum ada lokasi yang dibagikan device."
                        )

                        .setPositiveButton(
                            "OK",
                            null
                        )

                        .show()

                    return@addOnSuccessListener

                }


                val latitude =
                    document.getDouble(
                        "latitude"
                    )


                val longitude =
                    document.getDouble(
                        "longitude"
                    )


                val message =
                    "Latitude: $latitude\n\n" +
                    "Longitude: $longitude"


                AlertDialog.Builder(this)

                    .setTitle("DEVICE LOCATION")

                    .setMessage(message)

                    .setPositiveButton(
                        "OK",
                        null
                    )

                    .show()

            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Gagal mengambil lokasi",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }


    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroy() {

        deviceListener?.remove()

        super.onDestroy()

    }

}
