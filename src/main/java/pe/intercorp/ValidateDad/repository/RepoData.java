package pe.intercorp.ValidateDad.repository;

import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import pe.intercorp.ValidateDad.entity.Sku;
import pe.intercorp.ValidateDad.mapper.SkuMapper;

@Repository
public class RepoData {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert_sku_stg(String sku, String marca, String codbar) {
        String sql = """
                INSERT INTO EINTERFACE.IFH_SKU_STG_DAD
                (PRD_LVL_NUMBER,
                 COD_MARCA,    
                 PRD_UPC
                )
                VALUES
                (?,
                ?,
                ?
                )
                """;
        var i = jdbcTemplate.update(sql, sku, marca, codbar);
    }

    public void update_sku_arch(){
        String sql = """
                    UPDATE EINTERFACE.IFH_SKU_STG_DAD
                    SET STATUS=999,
                    DOWNLOAD_DATE=SYSDATE
                    WHERE STATUS IS NULL
                    AND DOWNLOAD_DATE IS NULL
                """;
        var i = jdbcTemplate.update(sql);
    }

    public void insert_sku_validate(String sku, String marca, String codbar) {
        String sql = """
                    INSERT INTO EINTERFACE.IFH_SKU_STG_DAD
                    (PRD_LVL_NUMBER,
                    COD_MARCA,    
                    PRD_UPC,
                    STATUS,
                    DOWNLOAD_DATE
                    )
                    VALUES
                    (?,
                    ?,
                    ?,
                    '200',
                    SYSDATE
                    )
                """;
        var i = jdbcTemplate.update(sql, sku, marca, codbar);
    }

    public void process_files_dad() {
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SP_PROCESS_FILES");
        simpleJdbcCall.execute();
    }

    public List<Sku> getSkus() {
        /// Se filtró la data con lo procesado hoy

        List<Sku> result = null;

        String query = """
                        SELECT RTRIM(P.PRD_LVL_NUMBER) SKU,
                        RTRIM(NVL(P.COD_MARCA,'')) MARCA,
                        RTRIM(NVL(P.COD_BAR,'')) CODBAR
                        FROM IFHPRDMST P
                        WHERE P.PRD_STYLE_IND = 'F'
                        AND P.COD_EST NOT IN (5, 99, 4, 1)
                        AND NOT EXISTS (SELECT 1
                        FROM EINTERFACE.IFH_SKU_STG_DAD X
                        WHERE X.PRD_LVL_NUMBER = P.PRD_LVL_NUMBER)
                        AND NOT (P.COD_BAR = '0' AND P.COD_EST = 0)
                        AND LENGTH(P.COD_BAR) = 13
                        AND P.FEC_CRE BETWEEN TO_DATE('20200101','YYYYMMDD')
                        AND TRUNC(SYSDATE)
                            """;
        result = jdbcTemplate.query(query,
                new Object[] {},
                new int[] {},
                new SkuMapper());

        return result;
    }


    public List<Sku> getCreationArch() {
        List<Sku> result = null;

        String query = """
                        SELECT RTRIM(P.PRD_LVL_NUMBER) SKU,
                        RTRIM(NVL(P.COD_MARCA,'')) MARCA,
                        RTRIM(NVL(P.COD_BAR,'')) CODBAR
                        FROM IFHPRDMST P
                        WHERE P.PRD_STYLE_IND = 'F'
                        AND P.COD_EST NOT IN (5, 99, 4, 1)
                        AND EXISTS (SELECT 1
                        FROM EINTERFACE.IFH_SKU_STG_DAD X
                        WHERE X.PRD_LVL_NUMBER = P.PRD_LVL_NUMBER
                        AND X.STATUS IS NULL)
                        AND NOT (P.COD_BAR = '0' AND P.COD_EST = 0)
                        AND LENGTH(P.COD_BAR) = 13
                            """;
        result = jdbcTemplate.query(query,
                new Object[] {},
                new int[] {},
                new SkuMapper());

        return result;
    }
}