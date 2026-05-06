package es.uc3m.fintech.lesson9.model;

/**
 * Tradable instrument: numeric id plus human-readable symbol.
 */
public class Instrument {
    private int instrumentId;
    private String symbol;

    public int getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(int instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instrument that = (Instrument) o;

        if (instrumentId != that.instrumentId) {
            return false;
        }
        return symbol != null ? symbol.equals(that.symbol) : that.symbol == null;
    }

    @Override
    public int hashCode() {
        int result = instrumentId;
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        return result;
    }
}
