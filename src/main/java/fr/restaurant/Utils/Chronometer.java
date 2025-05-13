package fr.restaurant.Utils;

import javafx.application.Platform;

public class Chronometer extends Thread {
    private int value;
    private int defaultMinutes = 25; // Temps par défaut en minutes
    private boolean running;
    private Runnable onUpdate;

    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); // Attendre 1 seconde
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            synchronized (this) {
                if (running) {
                    value--;
                    if (onUpdate != null) {
                        Platform.runLater(onUpdate);
                    }
                    if (value <= 0) {
                        System.out.println("Chronomètre terminé !");
                        running = false;
                        value = defaultMinutes * 60; // Réinitialiser le temps
                        if (onUpdate != null) {
                            Platform.runLater(onUpdate); // Mettre à jour l'interface
                        }
                        break;
                    }
                }
            }
        }
    }

    public synchronized void startChronometer(int minutes) {
        this.defaultMinutes = minutes; // Définir la valeur par défaut
        this.value = minutes * 60; // Convertir les minutes en secondes
        this.running = true;
        if (!this.isAlive()) {
            this.start(); // Démarrer le thread si ce n'est pas déjà fait
        }
    }

    public synchronized int getValue() {
        return value;
    }
}