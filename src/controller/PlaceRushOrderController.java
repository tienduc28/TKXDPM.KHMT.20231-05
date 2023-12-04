package controller;

import entity.shipping.Shipment;

import java.util.HashMap;
import java.util.logging.Logger;
/**
 * This class controls the flow of place rush order usecase in our AIMS project
 */
public class PlaceRushOrderController extends BaseController {
    /**
     * Just for logging purpose
     */
    private static Logger LOGGER = utils.Utils.getLogger(PlaceRushOrderController.class.getName());


    /**
     * @param deliveryData
     * @param typeDelivery
     */
    // control coupling
    public static void validatePlaceRushOrderData(HashMap<String, String> deliveryData, int typeDelivery) {
        if (typeDelivery == utils.Configs.PLACE_RUSH_ORDER) {
            Shipment shipment = new Shipment(typeDelivery, deliveryData.get("deliveryInstruction"), deliveryData.get("shipmentDetail"), deliveryData.get("deliveryTime"));
        }
    }
}
