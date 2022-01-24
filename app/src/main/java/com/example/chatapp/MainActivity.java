package com.example.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private MessagesAdapter messagesAdapter;

    private EditText editTextMessage;
    private ImageView imageViewSendMessage;
    private ImageView imageViewAddPicture;

    private List<Message> messageList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference storageReferenceToImages;
    private ActivityResultLauncher<Intent> signInLauncher;
    private ActivityResultLauncher<Intent> getImageActivityResultLauncher;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreateSetValues();
        setRecyclerViewMessages();

        imageViewSendMessage.setOnClickListener(view -> sendMessage(editTextMessage.getText().toString().trim(), null));
        imageViewAddPicture.setOnClickListener(view -> uploadImageFromLocalStorage());

        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(this, "Logged", Toast.LENGTH_SHORT).show();

        } else {
            signOut();
        }
        recyclerViewMessages.scrollToPosition(messagesAdapter.getItemCount() - 1);
    }

    private void uploadImageFromLocalStorage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        getImageActivityResultLauncher.launch(intent);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        listenForMessagesFromDb();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemSignOut) {
            mAuth.signOut();
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                preferences.edit().putString("author", mAuth.getCurrentUser().getEmail()).apply();
            }
        } else {
            if (response != null) {
                Toast.makeText(this, "Error: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listenForMessagesFromDb() {
        db.collection("messages").orderBy("date").addSnapshotListener((value, error) -> {
            if (value != null) {
                List<Message> messageList = value.toObjects(Message.class);
                messagesAdapter.setMessageList(messageList);
                recyclerViewMessages.scrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessage(String textOfMessage, String urlToImage) {

        Message message = null;
        String author = preferences.getString("author", "Anonymous");

        if (textOfMessage != null && !textOfMessage.isEmpty()) {
            message = new Message(author, textOfMessage, System.currentTimeMillis(), null);
        } else if (urlToImage != null && !urlToImage.isEmpty()) {
            message = new Message(author, textOfMessage, System.currentTimeMillis(), urlToImage);
        } else {
            return;
        }
        db.collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void setRecyclerViewMessages() {
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messagesAdapter);
    }

    private void onCreateSetValues() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        imageViewSendMessage = findViewById(R.id.imageViewSend);
        imageViewAddPicture = findViewById(R.id.imageViewAddImage);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        messagesAdapter = new MessagesAdapter(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance("gs://fir-start-b8ede.appspot.com");

        storageReference = storage.getReference();

        messageList = new ArrayList<>();

        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                this::onSignInResult
        );
        getImageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::uploadImageFromLocalStorage);
    }

    private void uploadImageFromLocalStorage(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Uri uri = data.getData();
                storageReferenceToImages = storageReference.child("images/" + uri.getLastPathSegment());
                storageReferenceToImages.putFile(uri).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return storageReferenceToImages.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            sendMessage(null, downloadUri.toString());
                        }
                    } else {
                        // Handle failures
                        // ...
                    }
                });
            }
        }
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                // Create and launch sign-in intent
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build();
                signInLauncher.launch(signInIntent);
            }
        });
    }
}
