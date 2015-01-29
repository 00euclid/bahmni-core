package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ETObsToBahmniObsMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniObservationTest {
    private EncounterTransaction.Observation eTObservation;

    @Mock
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        eTObservation = new EncounterTransaction.Observation();
        initMocks(this);
    }

    @Test
    public void shouldCreateBahmniObservationFromETObservation(){
        Date obsDate = new Date();
        EncounterTransaction.Concept concept = createConcept("concept-uuid", "concept-name");
        Concept conceptFromService = PowerMockito.mock(Concept.class);
        conceptFromService.setUuid("concept-uuid");
        ConceptName conceptNameFromService = new ConceptName();
        conceptNameFromService.setName("concept-name");

        when(conceptFromService.getName()).thenReturn(conceptNameFromService);
        when(conceptService.getConceptByUuid("concept-uuid")).thenReturn(conceptFromService);

        eTObservation = createETObservation("obs-uuid", "obs-value", concept, obsDate);

        eTObservation.addGroupMember(createETObservation("child-uuid", "child-value", concept, obsDate));

        BahmniObservation observation =  new ETObsToBahmniObsMapper(conceptService).create(eTObservation, new AdditionalBahmniObservationFields("encounter-uuid",new Date(),null,"obs-Group-Uuid"));
        assertEquals("comment", observation.getComment());
        assertEquals("obs-uuid", observation.getUuid());
        assertEquals("concept-uuid",observation.getConceptUuid());
        assertEquals("order-uuid", observation.getOrderUuid());
        assertEquals(obsDate,observation.getObservationDateTime());
        Collection<BahmniObservation> groupMembers = observation.getGroupMembers();
        assertEquals(1, groupMembers.size());
        assertEquals("obs-value",observation.getValue());
        assertEquals(true, observation.getVoided());
        assertEquals("chumma", observation.getVoidReason());
        assertEquals("encounter-uuid",observation.getEncounterUuid());
        assertEquals("obs-Group-Uuid",observation.getObsGroupUuid());

        BahmniObservation child = groupMembers.iterator().next();
        assertEquals("child-uuid", child.getUuid());
        assertEquals("child-value", child.getValue());
        assertEquals("encounter-uuid",child.getEncounterUuid());
    }

    @Test
    public void shouldReturnTrueIfBahmniObservationIsSameAsETObservation() throws Exception {
        eTObservation.setUuid("uuid");
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setUuid("uuid");

        boolean isSame = bahmniObservation.isSameAs(eTObservation);

        Assert.assertTrue(isSame);
    }

    @Test
    public void shouldConvertBahmniObservationToETObservation() throws Exception {
        Date obsDateTime = new Date();
        EncounterTransaction.Concept concept = createConcept("concept-uuid", "concept-name");
        BahmniObservation bahmniObservation = createBahmniObservation("obs-uuid","obs-value", concept,obsDateTime);
        bahmniObservation.addGroupMember(createBahmniObservation("child-uuid", "child-value", concept, obsDateTime));

        EncounterTransaction.Observation observation = bahmniObservation.toETObservation();
        
        assertEquals("comment",observation.getComment());
        assertEquals("obs-uuid",observation.getUuid());
        assertEquals("concept-uuid",observation.getConceptUuid());
        assertEquals("order-uuid",observation.getOrderUuid());
        assertEquals(obsDateTime,observation.getObservationDateTime());
        assertEquals(1,observation.getGroupMembers().size());
        assertEquals("obs-value",observation.getValue());
        assertEquals(true,observation.getVoided());
        assertEquals("chumma", observation.getVoidReason());
        assertEquals("child-uuid", observation.getGroupMembers().get(0).getUuid());
        assertEquals("child-value", observation.getGroupMembers().get(0).getValue());
    }

    private EncounterTransaction.Concept createConcept(String conceptUuid, String conceptName) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        concept.setName(conceptName);
        return concept;
    }

    private BahmniObservation createBahmniObservation(String uuid,String value,EncounterTransaction.Concept concept,Date obsDate) {
        BahmniObservation bahmniObservation1 = new BahmniObservation();
        bahmniObservation1.setUuid(uuid);
        bahmniObservation1.setValue(value);
        bahmniObservation1.setConcept(concept);
        bahmniObservation1.setComment("comment");
        bahmniObservation1.setObservationDateTime(obsDate);
        bahmniObservation1.setOrderUuid("order-uuid");
        bahmniObservation1.setVoided(true);
        bahmniObservation1.setVoidReason("chumma");
        return bahmniObservation1;
    }

    private EncounterTransaction.Observation createETObservation(String uuid,String value,EncounterTransaction.Concept concept,final Date obsDate) {
        EncounterTransaction.Observation etObservation = new EncounterTransaction.Observation();
        etObservation.setUuid(uuid);
        etObservation.setValue(value);
        etObservation.setConcept(concept);
        etObservation.setComment("comment");
        etObservation.setObservationDateTime(obsDate);
        etObservation.setOrderUuid("order-uuid");
        etObservation.setVoided(true);
        etObservation.setVoidReason("chumma");
        return etObservation;
    }
}
