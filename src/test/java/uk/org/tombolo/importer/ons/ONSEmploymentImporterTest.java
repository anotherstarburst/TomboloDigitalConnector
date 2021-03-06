package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * claimantsCount
 * Remote: "http://www.nomisweb.co.uk/api/v01/dataset/NM_162_1.data.csv?" +
 *      "geography=1249902593...1249937345&" +
 *      "date=latest&" +
 *      "gender=0&" +
 *      "age=0&" +
 *      "measure=1&" +
 *      "measures=20100&" +
 *      "select=date_name,geography_name,geography_code,gender_name,age_name,measure_name,measures_name,obs_value,obs_status_name";
 * Local: 41340fec-3141-3932-ae52-49477ff6c05d.csv
 *
 * JSAclaimantsCount
 * Remote: "http://www.nomisweb.co.uk/api/v01/dataset/NM_18_1.csv?geography=TYPE463&"+
 * "date=latest&"+
 * "sex=7&"+
 * "age=0&"+
 * "duration=0&"+
 * "measures=20100&"+
 * "select=date_name,geography_name,geography_code,measures_name,duration_name,sex_name,obs_value,obs_status_name"
 * Local: 8d68b4ed-7660-3540-ac06-e7bb76a91c70.csv
 *
 * JSAclaimantsProportion
 * Remote: "http://www.nomisweb.co.uk/api/v01/dataset/NM_18_1.csv?geography=TYPE463&"+
 * "date=latest&"+
 * "sex=7&"+
 *"age=0&"+
 * "duration=0&"+
 * "measures=20206&"+
 * "select=date_name,geography_name,geography_code,measures_name,duration_name,sex_name,obs_value,obs_status_name"
 * Local: 8ab4eaa0-a426-3228-9150-38b4e682ebaf.csv
 *
 * ESAclaimants
 * Remote: "http://www.nomisweb.co.uk/api/v01/dataset/NM_134_1.csv?geography=TYPE463&"+
 * "date=latest&"+
 * "sex=7&"+
 * "age=0&"+
 * "esa_phase=0&"+
 * "payment_type=0&"+
 * "icdgp_condition=0&"+
 * "duration=0&"+
 * "ethnic_group=0&"+
 * "measures=20100&"+
 * "select=date_name,geography_name,geography_code,measures_name,duration_name,sex_name,obs_value,obs_status_name"
 * Local: 4aa524ae-89f6-3cee-b09f-ab08e1bb35af.csv
 *
 */
public class ONSEmploymentImporterTest extends AbstractTest {
    public ONSEmploymentImporter importer;

    @Before
    public void before() throws Exception {
        importer = new ONSEmploymentImporter();
        mockDownloadUtils(importer);
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("claimantsCount","JSAclaimantsCount","JSAclaimantsProportion","ESAclaimants",
                "APSEmploymentRate", "APSUnemploymentRate","ONSJobsDensity","ONSTotalJobs","ONSGrossAnnualIncome"),datasources);
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("JSAclaimantsCount");
        assertEquals(9, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasourceClaimantsCount() throws Exception {

        SubjectType lsoa = TestFactory.makeNamedSubjectType("lsoa");
        Subject london001A = TestFactory.makeSubject(lsoa,"E01000001","City of London 001A",TestFactory.FAKE_POINT_GEOMETRY);
        Subject london001B = TestFactory.makeSubject(lsoa,"E01000002","City of London 001B",TestFactory.FAKE_POINT_GEOMETRY);
        Subject london001C = TestFactory.makeSubject(lsoa,"E01000003","City of London 001C",TestFactory.FAKE_POINT_GEOMETRY);

        Subject wyre010A = TestFactory.makeSubject(lsoa, "E01025542", "Wyre 010A", TestFactory.FAKE_POINT_GEOMETRY);
        Subject blaby010A = TestFactory.makeSubject(lsoa, "E01025613", "Blaby 010A", TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("claimantsCount", null, null, null);

        Attribute claimantsAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "claimantsCount");

        //Jan-16,City of London 001A,E01000001,Total,All categories: Age 16+,Claimant count,Value,0,Normal Value
        TimedValue londonValueA = TimedValueUtils.getLatestBySubjectAndAttribute(london001A, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),londonValueA.getId().getTimestamp());
        assertEquals(5d, londonValueA.getValue(), 0.1d);

        //Feb-16,City of London 001B,E01000002,Total,All categories: Age 16+,Claimant count,Value,0,Normal Value
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(london001B, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(0d, londonValueB.getValue(), 0.1d);

        //Feb-17,City of London 001C,E01000003,Total,All categories: Age 16+,Claimant count,Value,15,Normal Value
        TimedValue londonValueC = TimedValueUtils.getLatestBySubjectAndAttribute(london001C, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),londonValueC.getId().getTimestamp());
        assertEquals(15d, londonValueC.getValue(), 0.1d);

        //Jun-17,Wyre 010A,E01025542,Total,All categories: Age 16+,Claimant count,Value,5,Normal Value
        TimedValue wyreValue = TimedValueUtils.getLatestBySubjectAndAttribute(wyre010A, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),wyreValue.getId().getTimestamp());
        assertEquals(10d, wyreValue.getValue(), 0.1d);

        //December 2017,Blaby 010A,E01025613,Total,All categories: Age 16+,Claimant count,Value,5,Normal Value
        TimedValue blabyValue = TimedValueUtils.getLatestBySubjectAndAttribute(blaby010A, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),blabyValue.getId().getTimestamp());
        assertEquals(5d, blabyValue.getValue(), 0.1d);
    }
    @Test
    public void importDatasourceJSAclaimantsCount() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject cityofLondon = TestFactory.makeSubject(localAuthority,"E09000001","City of London",TestFactory.FAKE_POINT_GEOMETRY);
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("JSAclaimantsCount", null, null, null);

        Attribute claimantsAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "JSAclaimantsCount");

        //Jan-18,Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2018-01-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(2355d, londonValueB.getValue(), 0.1d);
    }
    @Test
    public void importDatasourceJSAclaimantsFraction() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject cityofLondon = TestFactory.makeSubject(localAuthority,"E09000001","City of London",TestFactory.FAKE_POINT_GEOMETRY);
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("JSAclaimantsProportion", null, null, null);

        Attribute claimantsAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "JSAclaimantsProportion");

        //Jan-18,City of London
        TimedValue londonValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityofLondon, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2018-01-31T23:59:59"),londonValue.getId().getTimestamp());
        assertEquals(0.4d, londonValue.getValue(), 0.01d);

        //Jan-18,Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2018-01-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(1.8d, londonValueB.getValue(), 0.01d);
    }
    @Test
    public void importDatasourceESAclaimants() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject cityofLondon = TestFactory.makeSubject(localAuthority,"E09000001","City of London",TestFactory.FAKE_POINT_GEOMETRY);
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("ESAclaimants", null, null, null);

        Attribute claimantsAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "ESAclaimants");

        //Jan-18,City of London
        TimedValue londonValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityofLondon, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-08-31T23:59:59"),londonValue.getId().getTimestamp());
        assertEquals(140d, londonValue.getValue(), 0.1d);

        //Jan-18,Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, claimantsAttribute);
        assertEquals(LocalDateTime.parse("2017-08-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(7550d, londonValueB.getValue(), 0.1d);
    }
    @Test
    public void importDatasourceAPSEmploymentRate() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("APSEmploymentRate", null, null, null);

        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "APSEmploymentRate");

        //Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, attribute);
        assertEquals(LocalDateTime.parse("2017-09-30T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(67d, londonValueB.getValue(), 0.1d);
    }
    @Test
    public void importDatasourceAPSUnemploymentRate() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("APSUnemploymentRate", null, null, null);

        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "APSUnemploymentRate");

        //Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, attribute);
        assertEquals(LocalDateTime.parse("2017-09-30T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(9.4d, londonValueB.getValue(), 0.1d);
    }
    @Test
    public void importDatasourceONSJobsDensity() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("ONSJobsDensity", null, null, null);

        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "ONSJobsDensity");

        //Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, attribute);
        assertEquals(LocalDateTime.parse("2016-12-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(0.49d, londonValueB.getValue(), 0.01d);
    }
    @Test
    public void importDatasourceONSTotalJobs() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("ONSTotalJobs", null, null, null);

        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "ONSTotalJobs");

        //Barking
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, attribute);
        assertEquals(LocalDateTime.parse("2016-12-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(64000d, londonValueB.getValue(), 0.01d);
    }
    @Test
    public void importDatasourceONSGrossAnnualIncome() throws Exception {

        SubjectType localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        Subject barkingAndDagenham = TestFactory.makeSubject(localAuthority,"E09000002","Barking and Dagenham",TestFactory.FAKE_POINT_GEOMETRY);

        importer.importDatasource("ONSGrossAnnualIncome", null, null, null);

        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "ONSGrossAnnualIncome");

        //Barking 2017
        TimedValue londonValueB = TimedValueUtils.getLatestBySubjectAndAttribute(barkingAndDagenham, attribute);
        assertEquals(LocalDateTime.parse("2017-12-31T23:59:59"),londonValueB.getId().getTimestamp());
        assertEquals(30167d, londonValueB.getValue(), 0.01d);
    }
}
