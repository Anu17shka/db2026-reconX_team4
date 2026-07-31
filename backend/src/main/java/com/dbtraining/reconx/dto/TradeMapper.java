package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Trade;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TradeMapper {

    @Mapping(source = "counterparty.id",
             target = "counterpartyId")
    @Mapping(source = "counterparty.name",
             target = "counterpartyName")
    @Mapping(source = "instrument.id",
             target = "instrumentId")
    @Mapping(source = "instrument.symbol",
             target = "instrumentSymbol")
    @Mapping(source = "status",
             target = "status",
             qualifiedByName = "statusToString")
    TradeResponse toResponse(Trade trade);


    @Mapping(target = "id",
             ignore = true)

    @Mapping(target = "counterparty",
             ignore = true)

    @Mapping(target = "instrument",
             ignore = true)

    @Mapping(target = "status",
             ignore = true)

    @Mapping(target = "assetClass",
             ignore = true)

    @Mapping(target = "side",
             ignore = true)

    @Mapping(target = "createdAt",
             ignore = true)

    @Mapping(target = "modifiedAt",
             ignore = true)
    Trade toEntity(TradeRequest req);


    @Named("statusToString")
    static String statusToString(String status) {
        return status;
    }
}