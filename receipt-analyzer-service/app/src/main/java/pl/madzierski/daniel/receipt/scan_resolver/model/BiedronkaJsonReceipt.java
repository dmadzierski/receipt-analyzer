package pl.madzierski.daniel.receipt.scan_resolver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BiedronkaJsonReceipt(
    String protoVersion,
    @JsonProperty("IDZ") String iDZ,
    Integer deviceType,
    Boolean printed,
    String data,
    List<Header> header,
    List<Body> body,
    String sign
) {

    public record Header(Image image, HeaderText headerText, HeaderData headerData) {
        public record Image(String id, String hash, String data) {
        }

        public record HeaderText(String headerTextLines) {
        }

        public record HeaderData(String tin, Integer docNumber, String date, Integer CPS) {
        }
    }

    public record Body(
        SellLine sellLine,
        DiscountLine discountLine,
        DiscountSummary discountSummary,
        VatSummary vatSummary,
        SumInCurrency sumInCurrency,
        Payment payment,
        FiscalFooter fiscalFooter,
        AddLine addLine,
        Barcode barcode,
        SysNumber sysNumber,
        Section section
    ) {
        public record SellLine(String name, String vatId, Integer price, Integer total, String quantity,
                               Boolean isStorno) {
        }

        public record DiscountLine(Integer base, Integer value, Boolean isDiscount, Boolean isPercent, Boolean isStorno,
                                   String vatId) {
        }

        public record DiscountSummary(Integer discounts) {
        }

        public record VatSummary(String currency, List<VatRateSummary> vatRatesSummary) {
            public record VatRateSummary(String vatId, Integer vatRate, Integer vatSale, Integer vatAmount) {
            }
        }

        public record SumInCurrency(Integer fiscalTotal, Integer totalWithPacks, String currency, Boolean printBig,
                                    Boolean printable) {
        }

        public record Payment(String type, Integer amount, String name, String currency) {
        }

        public record FiscalFooter(Integer billNumber, String uniqueNumber, String cashNumber, String cashier,
                                   Integer CPS, String date) {
        }

        public record AddLine(Integer id, String data, Integer width, Integer CPS) {
        }

        public record Barcode(Integer id, String data) {
        }

        public record SysNumber(String data, Integer width, Integer CPS) {
        }

        public record Section(Integer type) {
        }
    }
}

