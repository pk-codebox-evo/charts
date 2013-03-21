package com.vaadin.addon.charts.model.gsonhelpers;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 2.0
 * (CVALv2).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv2 along with this program.
 * If not, see <http://vaadin.com/license/cval-2.0>.
 * #L%
 */

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vaadin.addon.charts.model.ContainerDataSeries;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Serializer for ContainerSeries
 */
public class ContainerDataSeriesSerializer implements
        JsonSerializer<ContainerDataSeries> {
    
    private enum Mode {
        ONLY_Y, XY, OBJECT
    }

    @Override
    public JsonElement serialize(ContainerDataSeries src, Type typeOfSrc,
            JsonSerializationContext context) {
        JsonObject series = new JsonObject();
        JsonArray data = new JsonArray();

        if (context != null) {
            // FIXME serialize custom Plot options stuff or verify this works
//            series.add("size", context.serialize(src.getSize()));
//            series.add("center", context.serialize(src.getCenter()));
//            series.add("dataLabels", context.serialize(src.getDataLabels()));
//            series.add("type", context.serialize(src.getType()));
//            series.add("innerSize", context.serialize(src.getInnerSize()));
//            series.add("showInLegend", context.serialize(src.isShowInLegend()));
//            series.add("pointPlacement",
//                    context.serialize(src.getPointPlacement()));
//            series.add("marker", context.serialize(src.getMarker()));
//            series.add("tooltip", context.serialize(src.getTooltip()));
//            series.add("color", context.serialize(src.getColor()));
//            series.add("states", context.serialize(src.getStates()));
//            series.add("enableMouseTracking",
//                    context.serialize(src.isEnableMouseTracking()));
            series.add("name", context.serialize(src.getName()));
            series.add("stack", context.serialize(src.getStack()));

        }
        series.add("data", data);

        Map<String, Object> pidMap = src.getAttributeToPropertyIdMap();

        
        Mode mode = null;
        for(Object o : pidMap.keySet()) {
            if(!(o.equals(ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE1) 
                    || o.equals(ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE2))) {
                mode = Mode.OBJECT;
                break;
            }
        }
        Object xProperty = pidMap
                .get(ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE1);
        if(xProperty == null) {
            xProperty = ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE1;
        }
        Object yProperty = pidMap
                .get(ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE2);
        if(yProperty == null) {
            yProperty = ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE2;
        }
        Container container = src.getVaadinContainer();
        if(mode != Mode.OBJECT) {
            if(container.getContainerPropertyIds().contains(xProperty)) {
                mode = Mode.XY;
            } else {
                mode = Mode.ONLY_Y;
            }
        }

        for (Object iid : container.getItemIds()) {
            Item item = container.getItem(iid);
            switch (mode) {
            case ONLY_Y:
                addAnonymousTypedValue(
                        data,
                        item.getItemProperty(yProperty));
                break;
            case XY:
                JsonArray entryArray = new JsonArray();
                data.add(entryArray);
                addAnonymousTypedValue(entryArray, item.getItemProperty(xProperty));
                addAnonymousTypedValue(entryArray, item.getItemProperty(yProperty));
                break;

            default:
                // render as json object
                JsonObject entryObject = new JsonObject();
                Property x = item.getItemProperty(xProperty);
                if (x != null) {
                    addNamedAndTypedValue(entryObject,
                            ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE1,
                            x);
                }
                Property y = item.getItemProperty(yProperty);
                if (y != null) {
                    addNamedAndTypedValue(entryObject,
                            ContainerDataSeries.SERIES_DEFAULT_ATTRIBUTE2,
                            y);
                }

                Iterator<String> iter = pidMap.keySet().iterator();
                while (iter.hasNext()) {
                    String name = iter.next();
                    Object id = pidMap.get(name);
                    if (!id.equals(xProperty) && !id.equals(yProperty)) {
                        addNamedAndTypedValue(entryObject, name,
                                item.getItemProperty(id));
                    }
                }
                data.add(entryObject);
                
                break;
            }
        }
        return series;
    }

    private void addAnonymousTypedValue(JsonArray data, Property<?> itemProperty) {
        Object value = itemProperty.getValue();
        if (itemProperty != null && value != null) {
            if (Number.class.isAssignableFrom(itemProperty.getType())) {
                Number pNum = (Number) itemProperty.getValue();
                data.add(new JsonPrimitive(pNum));
            } else if (Boolean.class.isAssignableFrom(itemProperty.getType())) {
                Boolean pBool = (Boolean) itemProperty.getValue();
                data.add(new JsonPrimitive(pBool));
            } else if (Date.class.isAssignableFrom(itemProperty.getType())) {
                Date date = (Date) itemProperty.getValue();
                data.add(new JsonPrimitive(date.getTime()));
            } else {
                String pStr = itemProperty.getValue().toString();
                data.add(new JsonPrimitive(pStr));
            }
        }
    }

    private void addNamedAndTypedValue(JsonObject entryObject, String name,
            Property<?> itemProperty) {
        if (itemProperty != null && itemProperty.getValue() != null) {
            if (Number.class.isAssignableFrom(itemProperty.getType())) {
                Number pNum = (Number) itemProperty.getValue();
                entryObject.add(name, new JsonPrimitive(pNum));
            } else if (Boolean.class.isAssignableFrom(itemProperty.getType())) {
                Boolean pBool = (Boolean) itemProperty.getValue();
                entryObject.add(name, new JsonPrimitive(pBool));
            } else {
                String pStr = itemProperty.getValue().toString();
                entryObject.add(name, new JsonPrimitive(pStr));
            }
        }
    }
}
