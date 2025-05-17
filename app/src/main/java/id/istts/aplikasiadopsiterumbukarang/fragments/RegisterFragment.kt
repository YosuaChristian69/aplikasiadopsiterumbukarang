package id.istts.aplikasiadopsiterumbukarang.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import android.view.animation.DecelerateInterpolator
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import id.istts.aplikasiadopsiterumbukarang.R

class RegisterFragment : Fragment() {

    // Declare the videoView property with lateinit
    private lateinit var videoView: VideoView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up video background
        setupVideoBackground(view)

        // Animate register card
        animateRegisterCard(view)

        // Set up click listeners
        setupClickListeners(view)
    }

    private fun setupVideoBackground(view: View) {
        // Initialize the videoView property
        videoView = view.findViewById(R.id.videoBackground)

        // Use a video from raw resource folder
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.coral_background_video
        val uri = Uri.parse(videoPath)

        videoView.setVideoURI(uri)

        // Set on prepared listener to start the video when ready
        videoView.setOnPreparedListener { mp ->
            // Configure video properties
            mp.isLooping = true

            // Mute the video (for background videos)
            mp.setVolume(0f, 0f)

            // Optional: If you want to scale the video to cover the entire view
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

            // Start playback
            videoView.start()
        }

        // Handle any errors that might occur
        videoView.setOnErrorListener { _, _, _ ->
            // Log error or handle it appropriately
            // For now, we'll just return true to prevent crash
            true
        }
    }

    private fun animateRegisterCard(view: View) {
        val registerCard = view.findViewById<CardView>(R.id.registerCard)

        registerCard.alpha = 0f
        registerCard.translationY = 100f

        registerCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupClickListeners(view: View) {
        val loginLink = view.findViewById<TextView>(R.id.loginLink)
        loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)
        registerButton.setOnClickListener {
            // Implement registration functionality here
            // For now, navigate to login
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    // Handle lifecycle events to properly manage video playback
    override fun onResume() {
        super.onResume()
        if (::videoView.isInitialized && !videoView.isPlaying) {
            videoView.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::videoView.isInitialized && videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }
}