package es.uc3m.fintech.lesson9.utils;

import es.uc3m.fintech.lesson9.model.Instrument;
import es.uc3m.fintech.lesson9.model.ReferenceData;
import es.uc3m.fintech.lesson9.proto.Lesson9;

import java.util.ArrayList;

/**
 * Builds the canonical benchmark payload: a {@code ReferenceData} carrying
 * {@code marketId=1}, {@code algorithmIdentifier="TWAP"} and five Spanish
 * blue-chip instruments (BBVA, SAN, FDAX, TEF, IBER).
 *
 * Both the POJO and the protobuf-generated representations are produced from
 * the same source values so the two serializers run on equivalent inputs.
 */
public final class Utils {

    private Utils() {
    }

    public static ReferenceData getReferenceData() {
        Instrument i1 = new Instrument();
        i1.setInstrumentId(1);
        i1.setSymbol("BBVA");
        Instrument i2 = new Instrument();
        i2.setInstrumentId(2);
        i2.setSymbol("SAN");
        Instrument i3 = new Instrument();
        i3.setInstrumentId(3);
        i3.setSymbol("FDAX");
        Instrument i4 = new Instrument();
        i4.setInstrumentId(4);
        i4.setSymbol("TEF");
        Instrument i5 = new Instrument();
        i5.setInstrumentId(5);
        i5.setSymbol("IBER");
        ArrayList<Instrument> instruments = new ArrayList<>();
        instruments.add(i1);
        instruments.add(i2);
        instruments.add(i3);
        instruments.add(i4);
        instruments.add(i5);
        ReferenceData referenceData = new ReferenceData();
        referenceData.setMarketId(1);
        referenceData.setAlgorithmIdentifier("TWAP");
        referenceData.setListOfInstruments(instruments);
        return referenceData;
    }

    public static Lesson9.ReferenceData getProtoReferenceData() {
        ReferenceData referenceData = Utils.getReferenceData();
        Lesson9.ReferenceData.Builder referenceDataBuilder = Lesson9.ReferenceData.newBuilder();
        referenceDataBuilder.setMarketId(referenceData.getMarketId())
                .setAlgorithmIdentifier(referenceData.getAlgorithmIdentifier());
        for (Instrument value : referenceData.getListOfInstruments()) {
            referenceDataBuilder.addInstrument(Lesson9.Instrument.newBuilder()
                    .setInstrumentId(value.getInstrumentId())
                    .setSymbol(value.getSymbol()));
        }
        return referenceDataBuilder.build();
    }
}
