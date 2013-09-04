package com.agileapes.dragonfly.statement.impl;

import com.agileapes.dragonfly.dialect.DatabaseDialect;
import com.agileapes.dragonfly.error.StatementError;
import com.agileapes.dragonfly.metadata.ConstraintMetadata;
import com.agileapes.dragonfly.metadata.TableMetadata;
import com.agileapes.dragonfly.statement.Statement;
import com.agileapes.dragonfly.statement.StatementBuilder;
import com.agileapes.dragonfly.statement.StatementType;
import com.agileapes.dragonfly.statement.impl.model.FreemarkerStatementModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Pattern;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/1, 1:25)
 */
public class FreemarkerStatementBuilder implements StatementBuilder {

    public static final Pattern STATEMENT_PATTERN = Pattern.compile("(%\\{.*?\\}|<%.*?>|</%.*>)", Pattern.DOTALL);
    public static final Pattern VALUE_PATTERN = Pattern.compile("(?:[%\\$]\\{[^\\}]*?\\b(?:value|new|old)|<[\\$%].*?\\b(?:value|new|old))");
    private final String templateName;
    private final Configuration configuration;
    private final DatabaseDialect dialect;
    private final ConstraintMetadata constraintMetadata;

    public FreemarkerStatementBuilder(Configuration configuration, String templateName, DatabaseDialect dialect) {
        this(configuration, templateName, dialect, null);
    }

    public FreemarkerStatementBuilder(Configuration configuration, String templateName, DatabaseDialect dialect, ConstraintMetadata constraintMetadata) {
        this.templateName = templateName;
        this.configuration = configuration;
        this.dialect = dialect;
        this.constraintMetadata = constraintMetadata;
    }

    @Override
    public Statement getStatement(TableMetadata<?> tableMetadata) {
        final Template template;
        try {
            template = configuration.getTemplate(templateName);
        } catch (IOException e) {
            throw new StatementError("Failed to load template: " + templateName, e);
        }
        final FreemarkerStatementModel model;
        try {
            model = new FreemarkerStatementModel(tableMetadata, dialect);
        } catch (TemplateModelException ignored) {
            return null;
        }
        model.introduce("constraint", constraintMetadata);
        final StringWriter writer = new StringWriter();
        try {
            template.process(model, writer);
        } catch (Exception ignored) {
        }
        final String sql = writer.toString().trim();
        return new ImmutableStatement(sql, STATEMENT_PATTERN.matcher(sql).find(), VALUE_PATTERN.matcher(sql).find(), StatementType.getStatementType(sql));
    }

}