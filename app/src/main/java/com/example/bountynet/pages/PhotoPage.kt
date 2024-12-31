import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bountynet.FirebaseHelper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

@Composable
fun PhotoPage(
    navController: NavController,
    userId: String
) {
    val capturedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    var uploadStatus by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // Launcher to take a photo
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap.value = bitmap
            // Start uploading the photo
            isUploading = true
            uploadPhotoToFirebase(
                bitmap = bitmap,
                userId = userId,
                onUploadComplete = { status ->
                    FirebaseHelper.concludeBounty(userId)
                    uploadStatus = status
                    isUploading = false
                }
            )
        }
    }

    // Automatically launch the camera when this screen opens
    LaunchedEffect(Unit) {
        takePhotoLauncher.launch(null)
    }

    // Get the background color from MaterialTheme
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)  // Apply background color from the theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),  // Optional padding
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUploading) {
                // Show loading screen with progress indicator and text
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
                Text(
                    text = "Uploading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                // Show photo preview after capture
                capturedBitmap.value?.let { bitmap ->
                    Text(
                        text = "Photo Captured!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = BitmapPainter(bitmap.asImageBitmap()),
                        contentDescription = "Captured Photo",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Show upload status message
                if (uploadStatus.isNotEmpty()) {
                    Text(
                        text = uploadStatus,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // Show a button after upload completes
                if (!isUploading) {
                    Button(
                        onClick = { navController.popBackStack() }, // Navigate to another screen
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

fun uploadPhotoToFirebase(bitmap: Bitmap, userId: String, onUploadComplete: (String) -> Unit) {
    val storage = Firebase.storage

    // Retrieve the currentBountyId for the user
    FirebaseHelper.getObjectAttribute(
        path = "users",
        id = userId,
        attribute = "currentBountyId",
        onSuccess = { bountyId ->
            val storageRef = storage.reference.child("photos/$bountyId.jpg")
            // Convert bitmap to byte array
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Upload the byte array
            storageRef.putBytes(data)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("PhotoUpload", "Photo uploaded successfully: $uri")
                        onUploadComplete("Upload Successful")
                        // Save the URI to Firestore/Database
                        savePhotoUrlToDatabase(bountyId.toString(), uri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PhotoUpload", "Upload failed", exception)
                    onUploadComplete("Upload Failed: ${exception.message}")
                }
        },
        onFailure = { error ->
            Log.e("PhotoUpload", "Error retrieving bountyId: $error")
            onUploadComplete("Error retrieving bountyId: $error")
        }
    )
}

fun savePhotoUrlToDatabase(bountyId: String, photoUrl: String) {
    val db = Firebase.firestore
    val photoData = mapOf("photoUrl" to photoUrl)

    db.collection("bounties").document(bountyId)
        .set(photoData) // Simplified: one document per bountyId
        .addOnSuccessListener {
            Log.d("Firestore", "Photo URL saved successfully for bountyId: $bountyId")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error saving photo URL", e)
        }
}
