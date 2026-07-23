package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LAST_FILE_UPLOADED_BY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class BailCaseUtilsTest {
    @Mock
    private BailCase bailCase;

    @Test
    void isAdmin_should_return_true() {
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(BailCaseUtils.isInternalCase(bailCase));
    }

    @Test
    void isAdmin_should_return_false() {
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(BailCaseUtils.isInternalCase(bailCase));
    }

    @Test
    void isFileUploadedByLegalRepOrHomeOffice_should_return_true_for_legal_representative() {
        when(bailCase.read(LAST_FILE_UPLOADED_BY, String.class)).thenReturn(Optional.of("Legal Representative"));
        assertTrue(BailCaseUtils.isFileUploadedByLegalRepOrHomeOffice(bailCase));
    }

    @Test
    void isFileUploadedByLegalRepOrHomeOffice_should_return_true_for_home_office() {
        when(bailCase.read(LAST_FILE_UPLOADED_BY, String.class)).thenReturn(Optional.of("Home Office"));
        assertTrue(BailCaseUtils.isFileUploadedByLegalRepOrHomeOffice(bailCase));
    }

    @Test
    void isFileUploadedByLegalRepOrHomeOffice_should_return_false_for_other_uploaders() {
        when(bailCase.read(LAST_FILE_UPLOADED_BY, String.class)).thenReturn(Optional.of("Tribunal"));
        assertFalse(BailCaseUtils.isFileUploadedByLegalRepOrHomeOffice(bailCase));
    }

    @Test
    void isFileUploadedByLegalRepOrHomeOffice_should_return_false_when_empty() {
        when(bailCase.read(LAST_FILE_UPLOADED_BY, String.class)).thenReturn(Optional.empty());
        assertFalse(BailCaseUtils.isFileUploadedByLegalRepOrHomeOffice(bailCase));
    }
}
