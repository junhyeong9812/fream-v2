package xyz.fream.api.global.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("uuidUtil 기능 테스트")
class UuidUtilsTest {

    @Test
    @DisplayName("UUID를 생성할 수 있다.")
    void should_generate_uuid() {
        // when
        String uuid = UuidUtils.generate();

        // then
        assertThat(uuid).isNotNull();
        assertThat(uuid).hasSize(36);
        assertThat(uuid).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("생성된 UUID는 매번 다르다.")
    void should_generate_different_uuids() {
        // when
        String uuid1 = UuidUtils.generate();
        String uuid2 = UuidUtils.generate();

        // then
        assertThat(uuid1).isNotEqualTo(uuid2);
    }

    @Test
    @DisplayName("유효한 UUID 문자열을 검증할 수 있다.")
    void should_validate_valid_uuid() {
        // given
        String validUuid = UuidUtils.generate();

        // when
        boolean isValid = UuidUtils.isValid(validUuid);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 uuid 문자열을 검증할 수 있다.")
    void should_validate_invalid_uuid() {
        // given
        String invalidUuid = "invalid-uuid";

        // when
        boolean isValid = UuidUtils.isValid(invalidUuid);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null UUID는 유효하지 않다.")
    void should_return_false_for_null_uuid() {
        // when
        boolean isValid = UuidUtils.isValid(null);

        // then
        assertThat(isValid).isFalse();
    }
}