package com.dev.rebook.vos;

public class ReviewPageVo {
    public final int rowCount;      // 한 페이지에 표시할 게시글의 개수
    public final int page;          // 사용자가 요청한 페이지 번호
    public final int totalCount;    // 전체 리뷰 개수
    public final int maxPage;       // 이동 가능한 최대 페이지 번호
    public final int dbOffset;      // SELECT 시 OFFSET 값

    public ReviewPageVo(int rowCount, int page, int totalCount) {
        this.rowCount = rowCount;
        this.totalCount = totalCount;
        this.maxPage = totalCount / rowCount + (totalCount % rowCount == 0 ? 0 : 1);
        this.page = Math.min(page, this.maxPage);
        this.dbOffset = Math.max(0, (this.page - 1) * rowCount);
    }
}