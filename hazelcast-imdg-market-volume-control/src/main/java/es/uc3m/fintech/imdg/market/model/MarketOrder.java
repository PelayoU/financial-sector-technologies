package es.uc3m.fintech.imdg.market.model;

import java.io.Serializable;

/**
 * Distributed-cache value type representing a single executed market order.
 *
 * Stored in the {@code ordenesMercado} {@code IMap} keyed by an order id;
 * carries the instrument symbol, the executed volume (shares) and the
 * price at which the order traded.
 */
public class MarketOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private String instrument;
    private Integer volume;
    private int price;

    public MarketOrder(String instrument, int volume, int price) {
        this.instrument = instrument;
        this.volume = volume;
        this.price = price;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
