package com.hemovia.api.service.usecase.query;

import com.hemovia.api.dto.request.QueryPage;
import com.hemovia.api.dto.response.PageResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class QueryPageTest {
    @Test
    void defaultsAndBoundaries() {
        assertThat(QueryPage.of(null, null)).isEqualTo(new QueryPage(0, 20));
        assertThat(QueryPage.of(3, 100)).isEqualTo(new QueryPage(3, 100));
        assertThat(QueryPage.of(0, 1).size()).isEqualTo(1);
        assertThatThrownBy(() -> QueryPage.of(-1, 20)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QueryPage.of(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QueryPage.of(0, 101)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pageMetadata() {
        var page = PageResponse.of(List.of("a"), 1, 20, 21);
        assertThat(page.content()).containsExactly("a");
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(20);
        assertThat(page.totalElements()).isEqualTo(21);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(PageResponse.of(List.of(), 0, 20, 0).totalPages()).isZero();
    }
}
