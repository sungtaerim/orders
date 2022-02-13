package com.lepse.orders.service;

import com.lepse.orders.models.OrderModel;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2007_01.DataManagement;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.*;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class FindOrder {

    private ServiceData serviceData;
    private DataManagementService dataManagementService;
    private OrderModel orderModel;

    private final CredentialManager credentialManager;
    private final String[] properties = new String[] {"le2_OtmIP", "le2_Shifrzip0", "le2_SrokIzgDate"};
    private final String searchName = "Item ID";
    private final String searchParam = "Item ID";

    @Autowired
    public FindOrder(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    /**
     * Changes the properties of an item's revision
     * @param itemId Item id
     * @return Result status
     * */
    public String changeProperties(String itemId) {

        try {
            com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults found = this.foundItem(itemId);

            int length = found.objectUIDS.length;
            String[] uids = new String[length];
            System.arraycopy(found.objectUIDS, 0, uids, 0, length);

            if (uids.length == 0) {
                throw new Exception("This item not exist");
            }

            serviceData = (ServiceData) dataManagementService.loadObjects(uids);
            ModelObject[] foundObjs = new ModelObject[serviceData.sizeOfPlainObjects()];
            for (int i = 0; i < serviceData.sizeOfPlainObjects(); i++) {
                foundObjs[i] = serviceData.getPlainObject(i);
            }
            ModelObject orderItem = foundObjs[0];
            ModelObject orderRevision = this.itemToRevision(orderItem);

            Map<String, DataManagement.VecStruct> currentProperties = this.setProperties();
            ServiceData returnData = dataManagementService.setProperties(new ModelObject[]{orderRevision}, currentProperties);

            if (returnData.sizeOfPartialErrors() > 0) {
                throw new Exception("Error: " + returnData.getPartialError(0).getMessages()[0]);
            }
            return "Properties were successfully updated";
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "Something went wrong";
    }

    /**
     * Finds an element by element id and saved queries name
     * @param code Item id
     * @return instance of SavedQuery.QueryResults
     * */
    private com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults foundItem(String code) {
        Connection connection = new Connection(credentialManager.getTcServer(), credentialManager, "REST", "HTTP");

        ImanQuery query = null;

        SavedQueryService queryService = SavedQueryService.getService(connection);
        dataManagementService = DataManagementService.getService(connection);
        try {
            SavedQuery.GetSavedQueriesResponse savedQueries = queryService.getSavedQueries();
            if (savedQueries.queries.length == 0) {
                throw new Exception("There are no saved queries in the system");
            }

            for (int i = 0; i < savedQueries.queries.length; i++) {
                if (savedQueries.queries[i].name.equals(searchName)) {
                    query = savedQueries.queries[i].query;
                    break;
                }
            }
            if (query == null) {
                throw new Exception("There is not an " + searchName + " query");
            }
        } catch (Exception ex) {
            System.out.println("GetSavedQueries service request failed");
            ex.printStackTrace();
        }
        com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[] savedQueryInput = new com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[1];
        savedQueryInput[0] = new com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput();
        savedQueryInput[0].query = query;
        savedQueryInput[0].entries = new String[1];
        savedQueryInput[0].values = new String[1];
        savedQueryInput[0].entries[0] = searchParam;
        savedQueryInput[0].values[0] = code;
        savedQueryInput[0].maxNumToReturn = 25;

        com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);

        return savedQueryResult.arrayOfResults[0];
    }

    /**
     * Finds an ItemRevision by Item
     * @param orderItem item instance of ModelObject
     * @return returns new instance of ModelObject with type LE2_ZakazOsnasRevision
     * */
    private ModelObject itemToRevision(ModelObject orderItem) {
        try {
            serviceData = dataManagementService.getProperties(new ModelObject[]{orderItem},
                    new String[]{"revision_list"});
            Property property = orderItem.getPropertyObject("revision_list");
            ModelObject[] revisions = property.getModelObjectArrayValue();

            System.out.println(revisions[0].getUid());

            serviceData = dataManagementService.getProperties(new ModelObject[]{revisions[revisions.length - 1]}, properties);

            return revisions[revisions.length - 1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return orderItem;
    }

    /**
     * Sets new itemRevision properties
     * @return a map of attribute names and values (string/VecStruct)
     * */
    private Map<String, DataManagement.VecStruct> setProperties() {
        Map<String, DataManagement.VecStruct> currentProperties = new HashMap<>();

        Class<?> thisClass = orderModel.getClass();
        List<Method> getters = Arrays.stream(thisClass.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get"))
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toList());
        try {
            for (int i = 0; i < properties.length; i++) {
                DataManagement.VecStruct vecStruct = new DataManagement.VecStruct();
                if (isString(getters.get(i))) {
                    vecStruct.stringVec = new String[]{(String) getters.get(i).invoke(orderModel)};
                } else {
                    vecStruct = this.toString(getters.get(i));
                }
                currentProperties.put(properties[i], vecStruct);
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
        }
        return currentProperties;
    }

    /**
     * Checks method name
     * @return true if method name contains 'String'
     * */
    private boolean isString(Method method) {
        return method.getReturnType().getName().contains("String");
    }

    private DataManagement.VecStruct toString(Method method) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String methodName = method.getReturnType().getName();
        DataManagement.VecStruct vecStruct = new DataManagement.VecStruct();
        try {
            if (methodName.contains("Date")) {
                vecStruct.stringVec = new String[]{format.format(orderModel.getOrderDate())};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return vecStruct;
    }
}
