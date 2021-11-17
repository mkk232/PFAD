package com.example.pfad1.mappers;

import com.example.pfad1.entities.board.ArticleEntity;
import com.example.pfad1.entities.board.BoardEntity;
import com.example.pfad1.entities.board.ImageEntity;
import com.example.pfad1.interfaces.IBoard;
import com.example.pfad1.vos.board.ImageUploadVo;
import com.example.pfad1.vos.board.ListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IBoardMapper {
    int insertArticle(ArticleEntity articleEntity);

    int insertImage(ImageUploadVo imageUploadVo);

    int deleteArticle(ArticleEntity articleEntity);

    ArticleEntity selectArticle(ArticleEntity articleEntity);

    ArticleEntity[] selectArticlesByList(ListVo listVo);

    int selectArticleCount(BoardEntity boardEntity);

    int selectArticleCountGreaterThan(ArticleEntity articleEntity);

    BoardEntity selectBoard(IBoard board);

    ImageEntity selectImage(ImageEntity imageEntity);

    int selectLastInsertId();

//    Article 지우지 않고 delete_flag = true로 변경
    int updateArticleDeleted(ArticleEntity articleEntity);

    int updateArticleView(ArticleEntity articleEntity);

}
