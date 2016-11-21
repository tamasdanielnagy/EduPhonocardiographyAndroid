package noise.phonocardiographygraph.gui;

import android.app.Application;

import noise.phonocardiographygraph.calculation.Phonocardiography;

/**
 * Created by Tamas on 2015.05.12..
 */
public class PhonocardApplication extends Application{
    private static PhonocardApplication ourInstance = new PhonocardApplication();
    private Phonocardiography phCard;

    public static PhonocardApplication getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        phCard = new Phonocardiography();
        ourInstance = this;
    }

    public Phonocardiography getPhCard() {
        return phCard;
    }
}
