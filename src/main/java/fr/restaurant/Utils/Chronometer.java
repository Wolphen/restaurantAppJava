package fr.restaurant.Utils;

import fr.restaurant.controller.OrderController;
import javafx.application.Platform;

/**
 * Un petit chrono façon pomodoro : démarre, s’arrête, relance
 * et déclenche OrderController.can/can’tOrder selon la minute.
 */
public class Chronometer extends Thread {

    // temps restant en secondes
    private int value;

    // la durée « officielle » (25 min par défaut)
    private int defaultMinutes = 25;

    // true → décrémente, false → pause/stop
    private volatile boolean running = false;

    // flag pour sortir proprement de run()
    private volatile boolean keepAlive = true;

    // callback UI (appelé toutes les secondes via Platform.runLater)
    private Runnable onUpdate;

    // pour ne déclencher l’alerte 15 min qu’une fois
    private int warned = 0;

    /*---------------------------------------------------*/
    /* setters                                           */
    /*---------------------------------------------------*/
    public void setOnUpdate(Runnable r) { this.onUpdate = r; }

    /*---------------------------------------------------*/
    /* Boucle du thread                                  */
    /*---------------------------------------------------*/
    @Override
    public void run() {
        while (keepAlive) {
            try {
                Thread.sleep(1_000);               // chaque seconde
            } catch (InterruptedException stop) {
                break;                              // on nous a réveillés : fin
            }

            if (!running) continue;                 // en pause ⇒ on boucle

            value--;

            // mise à jour UI
            if (onUpdate != null) Platform.runLater(onUpdate);

            // alerte 15 mn restantes
            if (value <= 15 * 60 && warned == 0) {
                warned = 1;
                OrderController.cantOrder();
            }

            // fin de chrono
            if (value <= 0) {
                running = false;
                warned  = 0;
                OrderController.canOrder();        // on ré-autorise les commandes
                value   = defaultMinutes * 60;     // prêt pour un nouveau round
                if (onUpdate != null) Platform.runLater(onUpdate);
            }
        }
    }

    /*---------------------------------------------------*/
    /* API publique                                      */
    /*---------------------------------------------------*/
    public synchronized void startChronometer(int minutes) {
        defaultMinutes = minutes;
        value          = minutes * 60;
        running        = true;
        warned         = 0;
        OrderController.canOrder();

        // si le thread n’est pas encore lancé, on le lance
        if (!isAlive()) start();
    }

    /** met le chrono en pause et remet le compteur à 0 */
    public synchronized void stopChronometer() {
        running = false;
        value   = 0;
        warned  = 0;
        OrderController.canOrder();
        if (onUpdate != null) Platform.runLater(onUpdate);
    }

    /** tue complètement le thread (irréversible) */
    public void kill() {
        keepAlive = false;
        interrupt();      // sort du sleep
    }

    public synchronized int getValue() { return value; }

    public synchronized boolean isRunning() { return running; }
}
