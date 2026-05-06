package es.uc3m.fintech.imdg.market.listener;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import es.uc3m.fintech.imdg.market.model.MarketOrder;

import java.io.Serializable;

/**
 * Cluster-aware listener that watches the {@code ordenesMercado} cache and
 * raises an alert every time the cumulative traded volume for a target
 * instrument crosses 30,000 shares since the previous alert.
 *
 * The listener filters by instrument so unrelated orders (e.g. Intel updates
 * when monitoring BBVA) don't pollute the running total. On {@code entryUpdated}
 * the previous volume is subtracted and the new one added — that way the
 * accumulator tracks the *delta* contributed by each modification rather than
 * double-counting in-place corrections from upstream.
 *
 * Implements {@link Serializable} because Hazelcast may ship the listener to
 * the partition owner depending on the listener registration mode.
 */
public class VolumeListener
        implements EntryAddedListener<String, MarketOrder>,
                   EntryUpdatedListener<String, MarketOrder>,
                   Serializable {

    private static final long serialVersionUID = 1L;
    private static final int VOLUME_ALERT_THRESHOLD = 30_000;

    private final String monitoredInstrument;
    private int accumulatedVolume = 0;

    public VolumeListener(String instrument) {
        this.monitoredInstrument = instrument;
    }

    @Override
    public void entryAdded(EntryEvent<String, MarketOrder> entryEvent) {
        MarketOrder order = entryEvent.getValue();
        if (!order.getInstrument().equals(monitoredInstrument)) {
            return;
        }

        accumulatedVolume += order.getVolume();
        triggerAlertIfNeeded();
    }

    @Override
    public void entryUpdated(EntryEvent<String, MarketOrder> entryEvent) {
        MarketOrder previous = entryEvent.getOldValue();
        MarketOrder current = entryEvent.getValue();

        // Keys are not bound to an instrument — an Ibex35 key may flip between symbols
        // across updates. Subtract only when the previous value is the monitored one;
        // add only when the new value is.
        if (monitoredInstrument.equals(previous.getInstrument())) {
            accumulatedVolume -= previous.getVolume();
        }
        if (monitoredInstrument.equals(current.getInstrument())) {
            accumulatedVolume += current.getVolume();
        }

        triggerAlertIfNeeded();
    }

    private void triggerAlertIfNeeded() {
        if (accumulatedVolume > VOLUME_ALERT_THRESHOLD) {
            accumulatedVolume = 0;
            System.out.println("Alerta: mas de " + VOLUME_ALERT_THRESHOLD + " unidades de "
                    + monitoredInstrument + " negociadas");
        }
    }
}
