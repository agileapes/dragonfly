SELECT * FROM ${qualify(table)} WHERE <#list table.primaryKey.columns as column>${qualify(column)} = ${value[column.propertyName]}<#if column_has_next> AND </#if></#list>