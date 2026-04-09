package org.smu.randsome.randsomeback.domain.member.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

class MyProfileTagsTest extends UnitTestSupport {

    @Test
    void 세_태그로_MyProfileTags를_생성한다() {
        MyProfileTags tags = MyProfileTags.create(PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE);

        assertThat(tags.personalityTag()).isEqualTo(PersonalityTag.ACTIVE);
        assertThat(tags.faceTypeTag()).isEqualTo(FaceTypeTag.PUPPY);
        assertThat(tags.datingStyleTag()).isEqualTo(DatingStyleTag.EXPRESSIVE);
    }

}