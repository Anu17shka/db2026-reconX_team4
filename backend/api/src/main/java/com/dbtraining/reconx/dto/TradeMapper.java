// // package com.dbtraining.reconx.dto;

// // import com.dbtraining.reconx.repository.entity.Trade;
// // import org.mapstruct.Mapper;
// // import org.mapstruct.Mapping;

// // /**
// //  * ============================================================================
// //  * TICKET-ADV054 — MapStruct mapper: Trade entity <-> DTO
// //  *
// //  * WHAT:    Generates the entity↔DTO conversion at compile time.
// //  * HOW:     componentModel="spring" → MapStruct emits a @Component bean named
// //  *          tradeMapper that you can @Autowire.
// //  * WHY:     Hand-written mappers drift. MapStruct fails the build if a new
// //  *          field is added to one side and forgotten on the other.
// //  * ============================================================================
// //  */
// // @Mapper(componentModel = "spring")
// // public interface TradeMapper {

// //     @Mapping(source = "instrument.id", target = "instrumentId")
// //     @Mapping(source = "instrument.symbol", target = "instrumentSymbol")
// //     @Mapping(source = "counterparty.id", target = "counterpartyId")
// //     @Mapping(source = "counterparty.name", target = "counterpartyName")
// //     TradeResponse toResponse(Trade trade);
// // }


// package com.dbtraining.reconx.dto;

// // import com.dbtraining.reconx.domain.Trade;
// import com.dbtraining.reconx.repository.entity.Trade;
// import org.mapstruct.*;

// @Mapper(componentModel = "spring",
//         unmappedTargetPolicy = ReportingPolicy.ERROR)
// public interface TradeMapper {

//     @Mapping(source = "counterparty.id",       target = "counterpartyId")
//     @Mapping(source = "counterparty.name",     target = "counterpartyName")
//     @Mapping(source = "instrument.id",         target = "instrumentId")
//     @Mapping(source = "instrument.symbol",     target = "instrumentSymbol")
//     @Mapping(source = "status",                target = "status",
//              qualifiedByName = "statusToString")
//     TradeResponse toResponse(Trade trade);

//     @Mapping(target = "id",            ignore = true)
//     @Mapping(target = "counterparty",  ignore = true)   // wired by service from id
//     @Mapping(target = "instrument",    ignore = true)
//     @Mapping(target = "status",        ignore = true)   // defaulted to PENDING
//     @Mapping(target = "createdAt",     ignore = true)
//     @Mapping(target = "modifiedAt",    ignore = true)
//     Trade toEntity(TradeRequest req);

//     @Named("statusToString")
//     static String statusToString(Enum<?> status) {
//         return status == null ? null : status.name();
//     }
// }

package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Trade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;


/**
 * MapStruct mapper for converting Trade entity and DTO objects.
 *
 * WHAT:
 * Converts Trade entity objects into DTOs and DTOs back into entities.
 *
 * HOW:
 * Uses MapStruct to generate mapping implementation during compilation.
 *
 * WHY:
 * Keeps conversion logic consistent and avoids manual mapping code.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TradeMapper {


    /**
     * Converts Trade entity into TradeResponse DTO.
     *
     * @param trade trade entity
     * @return mapped TradeResponse object
     */
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



    /**
     * Converts TradeRequest DTO into Trade entity.
     *
     * @param req trade request DTO
     * @return mapped Trade entity
     */
    @Mapping(target = "id",
             ignore = true)

    @Mapping(target = "counterparty",
             ignore = true)

    @Mapping(target = "instrument",
             ignore = true)

    @Mapping(target = "status",
             ignore = true)

    @Mapping(target = "createdAt",
             ignore = true)

    @Mapping(target = "modifiedAt",
             ignore = true)

    @Mapping(target = "assetClass",
             ignore = true)

    @Mapping(target = "side",
             ignore = true)

    Trade toEntity(TradeRequest req);



    /**
     * Converts status value into String.
     *
     * @param status trade status
     * @return status string
     */
    @Named("statusToString")
    static String statusToString(String status) {

        return status;

    }

}