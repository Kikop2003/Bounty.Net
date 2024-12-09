package com.example.bountynet.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bountynet.FirebaseHelper
import com.example.bountynet.Objects.User
import com.example.bountynet.Objects.UserSession

@Composable
fun Profile(modifier: Modifier = Modifier){
    val username = UserSession.username

    var user = User()

    FirebaseHelper.getUser(
        username = username,
        onSuccess = { returnUser ->
            if (returnUser != null) {
                println("User retrieved: ${returnUser.name}")
                user = returnUser
            } else {
                println("User not found")
            }
        },
        onFailure = { error ->
            println("Failed to retrieve user: $error")
        }
    )

    Column (horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Profile", modifier = modifier)

        //Image()

        Text(text = user.name, modifier = modifier)

        Row (){
            Button(onClick = { }) { //Add MyBounties OnClick
                Text("My Bounties")
            }

            Button(onClick = { }) { //Add Settings OnClick
                Text("Settings")
            }
        }

        //Stats
        Text("Completed Bounties: " + user.CompletedBounties) //get value from fb
        Text("Average time to complete: " + user.Averagetime)

        //CREATE PIE CHART


    }


}