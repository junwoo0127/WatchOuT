/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ssafy.homealone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.*
import com.ssafy.homealone.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


/**
 * Activity displaying the app UI. Notably, this binds data from [MainViewModel] to views on screen,
 * and performs the permission check when enabling measure data.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val viewModel: MainViewModel by viewModels()

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the NotificationChannel
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channelId = "alarm_1"

        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

//       ????????? ?????? ??????
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emergencyBtn = findViewById<Button>(R.id.emergencyButton)

        emergencyBtn.setOnLongClickListener {
            Toast.makeText(applicationContext, "?????? ????????? ?????????????????????!", Toast.LENGTH_SHORT).show()
            sendMessage(nodeClient, messageClient)
            true
        }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                when (result) {
                    true -> {
                        Log.i(TAG, "Body sensors permission granted")
                        // Only measure while the activity is at least in STARTED state.
                        // MeasureClient provides frequent updates, which requires increasing the
                        // sampling rate of device sensors, so we must be careful not to remain
                        // registered any longer than necessary.
//                        ?????? ??????????????? ????????????
//                        lifecycleScope.launchWhenStarted {
//                            viewModel.measureHeartRate()
//                        }
                    }
                    false -> Log.i(TAG, "Body sensors permission not granted")
                }
            }

        // ????????????????????? ?????????????????? ????????? ????????? ????????????
//        val service = Executors.newSingleThreadScheduledExecutor()
//        val handler = Handler(Looper.getMainLooper())
//        service.scheduleAtFixedRate({
//            handler.run {
//                // Do your stuff here
////                Log.d("5????????? ??????", Date().time.toString())
////            sendHeartRateMessage(nodeClient, messageClient, viewModel.heartRateBpm.value)
////                sendHeartRateData(dataClient, viewModel.heartRateBpm.value)
//            }
//        }, 0, 5, TimeUnit.SECONDS)

        // Bind viewmodel state to the UI.
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect {
                updateViewVisiblity(it)
            }
        }
//        ????????? ????????? ???????????? ????????? ??????
        lifecycleScope.launchWhenStarted {
            viewModel.heartRateBpm.collect {
                binding.lastMeasuredValue.text = String.format("%d", it.toInt())
//                ???????????? 0??? ????????? ????????? ????????? ????????? ??????
                if (it.toInt() != 0) {
                    sendHeartRateData(dataClient, it)
                }
            }
        }

//        // ????????? ????????? ????????? ?????? ??????
        lifecycleScope.launchWhenStarted {
            viewModel.lastHeartRate()
//            sendHeartRateData(dataClient, viewModel.heartRateBpm.value)
//            sendHeartRateMessage(nodeClient, messageClient, viewModel.heartRateBpm.value)
        }
    }

    override fun onStart() {
        super.onStart()
        permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
    }

    private fun updateViewVisiblity(uiState: UiState) {
        (uiState is UiState.Startup).let {
            binding.progress.isVisible = it
        }
        // These views are visible when heart rate capability is not available.
        (uiState is UiState.HeartRateNotAvailable).let {
            binding.brokenHeart.isVisible = it
            binding.notAvailable.isVisible = it
        }
        // These views are visible when the capability is available.
        (uiState is UiState.HeartRateAvailable).let {
            binding.lastMeasuredValue.isVisible = it
            binding.heart.isVisible = it
        }
    }


//    private fun onQueryOtherDevicesClicked() {
//        lifecycleScope.launch {
//            try {
//                val nodes = getCapabilitiesForReachableNodes()
//                    .filterValues { "mobile" in it || "wear" in it }.keys
//                displayNodes(nodes)
//            } catch (cancellationException: CancellationException) {
//                throw cancellationException
//            } catch (exception: Exception) {
//                Log.d(TAG, "Querying nodes failed: $exception")
//            }
//        }
//    }
//
//    private fun onQueryMobileClicked() {
//        lifecycleScope.launch {
//            try {
//                val nodes = getCapabilitiesForReachableNodes()
//                    .filterValues { "mobile" in it }.keys
//                displayNodes(nodes)
//            } catch (cancellationException: CancellationException) {
//                throw cancellationException
//            } catch (exception: Exception) {
//                Log.d(TAG, "Querying nodes failed: $exception")
//            }
//        }
//    }

    /**
     * Collects the capabilities for all nodes that are reachable using the [CapabilityClient].
     *
     * [CapabilityClient.getAllCapabilities] returns this information as a [Map] from capabilities
     * to nodes, while this function inverts the map so we have a map of [Node]s to capabilities.
     *
     * This form is easier to work with when trying to operate upon all [Node]s.
     */
//    private suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> =
//        capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE)
//            .await()
//            // Pair the list of all reachable nodes with their capabilities
//            .flatMap { (capability, capabilityInfo) ->
//                capabilityInfo.nodes.map { it to capability }
//            }
//            // Group the pairs by the nodes
//            .groupBy(
//                keySelector = { it.first },
//                valueTransform = { it.second }
//            )
//            // Transform the capability list for each node into a set
//            .mapValues { it.value.toSet() }
//
//    private fun displayNodes(nodes: Set<Node>) {
//        val message = if (nodes.isEmpty()) {
//            getString(R.string.no_device)
//        } else {
//            getString(R.string.connected_nodes, nodes.joinToString(", ") { it.displayName })
//        }
//
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val channelName = "watch_connectivity"

        //????????? ????????????
        fun sendHeartRateData(dataClient: DataClient, data: Any) {
            Log.d("????????? ?????? ??????", data.toString())
            val eventData = objectToBytes(mutableMapOf(Pair("HEART_RATE", data.toString().trim())))
            val dataItem = PutDataRequest.create("/$channelName")
            dataItem.data = eventData
            dataClient.putDataItem(dataItem)
                .addOnSuccessListener { Log.d("????????? ?????? ??????", data.toString()) }
                .addOnFailureListener {  Log.e("????????? ?????? ??????", it.message.toString()) }
        }

        //????????? ????????????
//        fun sendHeartRateMessage(nodeClient: NodeClient, messageClient: MessageClient, heartRate: Double) {
//            Log.d("????????? ????????? ?????? ??????", heartRate.toString())
//            val messageData = objectToBytes(mutableMapOf(Pair("HEART_RATE", heartRate.toString().trim())))
//
//            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
//                nodes.forEach {
//                    messageClient.sendMessage(it.id, channelName, messageData)
//                    Log.d("????????? ??????", objectFromBytes(messageData).toString())
//
////                messageClient.sendMessage(it.id, "/$channelName", messageData)
//                }
//            }
//        }

        // ????????? ??????
        private fun sendMessage(nodeClient: NodeClient, messageClient: MessageClient) {
            val messageData = objectToBytes(mutableMapOf(Pair("EMERGENCY", "?????? ???????????????!")))

            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                nodes.forEach {
                    messageClient.sendMessage(it.id, channelName, messageData)
                    Log.d("????????? ??????", objectFromBytes(messageData).toString())

//                messageClient.sendMessage(it.id, "/$channelName", messageData)
                }
            }
        }

        private fun objectFromBytes(bytes: ByteArray): Any {
            val bis = ByteArrayInputStream(bytes)
            val ois = ObjectInputStream(bis)
            return ois.readObject()
        }

        private fun objectToBytes(`object`: Any): ByteArray {
            val baos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(baos)
            oos.writeObject(`object`)
            return baos.toByteArray()
        }
    }
}
