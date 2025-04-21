package me.taromati.doneconnector;

/**
 * Interface for handling donation events from different platforms.
 */
public interface DonationListener {

    /**
     * Called when a donation is received from any platform.
     *
     * @param platform The platform name (e.g., "Chzzk", "Soop")
     * @param streamerTag The streamer's tag (Minecraft nickname)
     * @param donorNickname The donor's nickname
     * @param amount The donation amount
     * @param message The donation message
     */
    void onDonation(String platform, String streamerTag, String donorNickname, int amount, String message);
}
