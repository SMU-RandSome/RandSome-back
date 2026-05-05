package org.smu.randsome.randsomeback.domain.member.dto.command;

public record MemberSearchCondition(String keyword) {

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

}
