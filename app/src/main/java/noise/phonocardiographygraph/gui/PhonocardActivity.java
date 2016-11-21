package noise.phonocardiographygraph.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import noise.phonocardiographygraph.R;
import noise.phonocardiographygraph.audio.PhonocardCapture;
import noise.phonocardiographygraph.calculation.Phonocardiography;


public class PhonocardActivity extends ActionBarActivity {


    final int ACTIVITY_CHOOSE_FILE = 1;

    Graph graphHeartSound;
    Graph graphHeartRate;
    Phonocardiography phCard;
    PhonocardCapture capture;


    TextView pulseText;
    TextView meanRRText;
    TextView sdRRText;
    TextView rMSSDText;
    TextView pNN50Text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonocard);

        phCard = ((PhonocardApplication) getApplicationContext()).getPhCard();
        phCard.setLoaded(true);
        capture = new PhonocardCapture(phCard.getHeartSound());

        graphHeartSound = (Graph) findViewById(R.id.graph_heart_sound);
        graphHeartRate = (Graph) findViewById(R.id.graph_heart_rate);

        graphHeartSound.setDefaultScaling(0.0, 10.0, -1.0, 1.0);
        graphHeartRate.setDefaultScaling(0.0, 10.0, 50.0, 100.0);

        graphHeartSound.setParamCurve(phCard.getHeartSound());
        graphHeartSound.addSignal(phCard.getHeartSound());
        graphHeartSound.addSignal(phCard.getBeats());

        graphHeartRate.setParamCurve(phCard.getBeatsPerMinute());
        graphHeartRate.addSignal(phCard.getBeatsPerMinute());



        pulseText = (TextView) findViewById(R.id.textViewPulse);
        meanRRText = (TextView) findViewById(R.id.textViewmeanRR);
        sdRRText = (TextView) findViewById(R.id.textViewsdRR);
        rMSSDText = (TextView) findViewById(R.id.textViewrMSSD);
        pNN50Text = (TextView) findViewById(R.id.textViewpNN50);


        redrawGUI();

    }

    public void onButtonClicked(View view) {
        showChooser();

    }


    private void showChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", "file/*");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, ACTIVITY_CHOOSE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    phCard.reset();
                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(uri);
                        capture.readWavFile(is);
                    } catch (IOException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(this)
                                .setTitle("File read error")
                                .setMessage(e.getMessage())
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .show();
                    } finally {
                        if (is!= null)
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                    phCard.runCalculations();
                    redrawGUI();
                }
            }
        }
    }

    private void redrawGUI() {
        if (graphHeartSound != null && graphHeartRate != null) {
            graphHeartSound.postInvalidate();
            graphHeartRate.postInvalidate();
            pulseText.setText(String.format(Locale.US, "%.0f BPM", phCard.getPulse()));
            meanRRText.setText(String.format(Locale.US, "%d ms", phCard.getMeanRR()));
            sdRRText.setText(String.format(Locale.US, "%d ms", phCard.getsdRR()));
            rMSSDText.setText(String.format(Locale.US, "%d", phCard.getrMSSD()));
            pNN50Text.setText(String.format(Locale.US, "%.1f %%", phCard.getpNN50()));
        }
    }


}
