<#-- @ftlvariable name="" type="com.agileapes.dragonfly.model.EntityHandlerModel" -->
package ${entityType.canonicalName?substring(0, entityType.canonicalName?last_index_of("."))};

import com.agileapes.dragonfly.entity.EntityHandler;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.annotation.Generated;
import java.io.Serializable;

@Generated(
    value = "Dragonfly",
    comments = "Entity map handler for ${entityType.canonicalName}"
)
public class ${entityType.simpleName}EntityHandler implements EntityHandler<${entityType.canonicalName}> {

    @Override
    public Class<${entityType.canonicalName}> getEntityType() {
        return ${entityType.canonicalName}.class;
    }

    @Override
    public Map<String, Object> toMap(${entityType.canonicalName} entity) {
        final Map<String, Object> map = new HashMap<String, Object>();
        <#list properties as property>
        Object value${property_index} = <#if property.declaringClass.canonicalName == entityType.canonicalName>entity<#else>((${property.declaringClass.canonicalName}) entity)</#if>.${property.getterName}();
        if (value${property_index} != null) {<#if property.foreignProperty??>
            value${property_index} = ((${property.foreignProperty.declaringClass.canonicalName}) value${property_index}).${property.foreignProperty.getterName}();
        <#else>

        </#if>
        <#if property.temporalType??>
            value${property_index} = new java.sql.${property.temporalType?string?lower_case?cap_first}(((java.util.Date) value${property_index}).getTime());
        </#if>
            map.put("${property.propertyName}", value${property_index});
        }
        </#list>
        return map;
    }

    @Override
    public ${entityType.canonicalName} fromMap(${entityType.canonicalName} entity, Map<String, Object> map) {
        <#list properties as property><#if property.foreignProperty??><#else>
        if (map.containsKey("${property.columnName}")) {
            <#if property.declaringClass.canonicalName == entityType.canonicalName>entity<#else>((${property.declaringClass.canonicalName}) entity)</#if>.${property.setterName}((${property.propertyType.canonicalName}) map.get("${property.columnName}"));
        }
        </#if></#list>
        return entity;
    }

    @Override
    public Serializable getKey(${entityType.canonicalName} entity) {
        <#if key??>return (Serializable) entity.${key.getterName}();
        <#else>
        throw new com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError(${entityType.canonicalName}.class);
        </#if>
    }

    @Override
    public void setKey(${entityType.canonicalName} entity, Serializable key) {
        <#if key??>entity.${key.setterName}((${key.propertyType.canonicalName}) key);
        <#else>
        throw new com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError(${entityType.canonicalName}.class);
        </#if>
    }

    @Override
    public boolean hasKey() {
        return <#if key??>true<#else>false</#if>;
    }

    @Override
    public boolean isKeyAutoGenerated() {
        return ${keyAutoGenerated?string};
    }

    @Override
    public void copy(${entityType.canonicalName} original, ${entityType.canonicalName} copy) {
    <#list properties as property>
        ((${property.declaringClass.canonicalName}) copy).${property.setterName}(((${property.declaringClass.canonicalName}) original).${property.getterName}());
    </#list>
    }

    @Override
    public String getKeyProperty() {
        <#if key??>return "${key.propertyName}";
        <#else>
        throw new com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError(${entityType.canonicalName}.class);
        </#if>
    }

    @Override
    public void prepareRelations(${entityType.canonicalName} entity) {
    }

    @Override
    public Collection<?> getRelatedItems(${entityType.canonicalName} entity) {
        return new ArrayList<Object>();
    }

}
