package se.kth.csc.stayawhile;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Willy on 2016-04-28.
 */
public class WearMessageHandler extends WearableListenerService{
    private static final String SENDING_QUEUE = "/saw_sendingqueue";
    private static final String SEND_UPDATE_REQUEST = "/saw_sendupdatepls";
    private static final String SEND_KICK_USER = "/saw_kickemlow";
    private static final String SEND_ATTEND_USER = "/saw_illhavealook";


    private GoogleApiClient mGoogleApiClient;
    private MainActivity mainActivity;


    public WearMessageHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }




    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        if( messageEvent.getPath().equalsIgnoreCase( "/saw_repl" ) ) {
            //String msg = messageEvent.getData();
            Log.i("DEV", "Got queue sent confirmation!");
        }
        else if (messageEvent.getPath().equalsIgnoreCase(SEND_UPDATE_REQUEST)){
            sendQueueToWear();
        }
        else if (messageEvent.getPath().equalsIgnoreCase(SEND_KICK_USER)){
            kickUser(messageEvent.getData());
        }
        else if (messageEvent.getPath().equalsIgnoreCase(SEND_ATTEND_USER)){
            attendUser(messageEvent.getData());
        } else {
            super.onMessageReceived( messageEvent );
        }
    }

    private void attendUser(byte[] data) {
        //TODO
    }

    private void kickUser(byte[] data) {
        //TODO
    }


    private void sendQueueToWear(){
        //TODO
        //use SENDING_QUEUE as path
    }








    private void initGoogleApi(){
        mGoogleApiClient = new GoogleApiClient.Builder(mainActivity)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    private String getBestNodeId(){
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    public void sendMessage( final String path, final byte[] data ) {

        new Thread( new Runnable() {
            @Override
            public void run() {
                while (mGoogleApiClient == null)
                    initGoogleApi();

                Log.i("DEVm", "Getting nodes!");
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                Log.i("DEVm", "Got " + nodes.getNodes().size() + " nodes!");
                for(Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, data ).await();
                    Log.i("DEVm", "Sent message!");
                }
            }
        }).start();
        Log.i("DEVm","Should've sent message!");
    }
    public GoogleApiClient getApi(){
        while (mGoogleApiClient == null)
            initGoogleApi();
        return mGoogleApiClient;
    }
    public void disconnectApi() {
        mGoogleApiClient.disconnect();
    }
}
