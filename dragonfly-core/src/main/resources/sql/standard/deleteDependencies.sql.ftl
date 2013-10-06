<#list metadata.localTable.columns as column><#if column.propertyName == metadata.propertyName><#assign localColumn=column/></#if></#list>DELETE FROM ${qualify(metadata.foreignTable)} WHERE ${qualify(metadata.foreignColumn)} IN (SELECT DISTINCT ${qualify(localColumn)} FROM ${qualify(metadata.localTable)} WHERE ${qualify(localColumn)} IS NOT NULL);