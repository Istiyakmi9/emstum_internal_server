package com.ems.buildorg.service;

import com.ems.buildorg.modal.DbConfiguration;
import com.ems.buildorg.modal.DbParameters;
import com.ems.buildorg.modal.FilterModel;
import com.ems.buildorg.modal.TrialRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompanyDemoService {
    @Autowired
    DbConfiguration databaseConfiguration;
    @Autowired
    ObjectMapper objectMapper;
    public List<TrialRequest> getCompanyTrialService(FilterModel filterModel) throws Exception {
        List<DbParameters> dbParameters = new ArrayList<>();
        filterModel.setSearchString("1=1 and t.IsProcessed = false");
        dbParameters.add(new DbParameters("_seacrhString", filterModel.getSearchString(), Types.VARCHAR));
        dbParameters.add(new DbParameters("_pageIndex", filterModel.getPageIndex(), Types.INTEGER));
        dbParameters.add(new DbParameters("_pageSize", filterModel.getPageSize(), Types.INTEGER));
        var result = executeProcedure("sp_trail_request_getall", dbParameters);

        return objectMapper.convertValue(result.get("#result-set-1"), new TypeReference<List<TrialRequest>>() { });
    }

    public TrialRequest getCompanyTrialByIdService(Long trialRequestId) throws Exception {
        try {
            List<DbParameters> dbParameters = new ArrayList<>();
            dbParameters.add(new DbParameters("_TrailRequestId", trialRequestId, Types.BIGINT));
            var result = executeProcedure("sp_trail_request_getby_id", dbParameters);
            var data = objectMapper.convertValue(result.get("#result-set-1"), new TypeReference<List<TrialRequest>>() { });
            return data.get(0);
        }
        catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private <T> Map<String, Object> executeProcedure(String procedureName, List<DbParameters> sqlParams) throws Exception {
        databaseConfiguration.setCatalog("ems_master");
        DataSource dataSource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(procedureName);

        Map<String, Object> paramSet = new HashMap<>();
        try {
            for (DbParameters dbParameters : sqlParams) {
                paramSet.put(dbParameters.parameter, dbParameters.value);
                simpleJdbcCall.addDeclaredParameter(
                        new SqlParameter(
                                dbParameters.parameter,
                                dbParameters.type
                        ));
            }

            return simpleJdbcCall.execute(paramSet);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        var catalog = databaseConfiguration.getCatalog();
        if (databaseConfiguration.getCatalog() == null || databaseConfiguration.getCatalog().isEmpty())
            catalog = "";

        dataSource.setDriverClassName(databaseConfiguration.getDriver());
        dataSource.setUrl(databaseConfiguration.getUrl() + catalog);
        dataSource.setUsername(databaseConfiguration.getUsername());
        dataSource.setPassword(databaseConfiguration.getPassword());
        dataSource.setCatalog(databaseConfiguration.getCatalog());
        return dataSource;
    }
}
