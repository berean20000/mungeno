package com.mungeno.app.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import com.mungeno.app.common.JDBCUtil;
import com.mungeno.app.model.DiaryVO;
import com.mungeno.app.model.ReplyVO;

@Repository("diaryDAO")
public class DiaryDAO {
	private Connection conn=null;
	private PreparedStatement pstmt=null;
	private ResultSet rs=null;
	private final String DIARY_SELECTONE="select * from mungeno_diary where did=?";
	private final String DIARY_SELECTALL="select * from mungeno_diary order by did desc";
	private final String DIARY_SELECTALL_F="select * from mungeno_diary where ";
	private final String DIARY_SELECTALL_B=" like '%'||?||'%' order by did desc";
	private final String DIARY_INSERT="insert into mungeno_diary(did,mid,dtitle,dwriter,dimg,dcontent,mregion) values ((select nvl(max(did),1000)+1 from mungeno_diary),?,?,?,?,?,?)";
	private final String DIARY_UPDATE="update mungeno_diary set dtitle=?,dcontent=? where did=?";
	private final String DIARY_UPDATE_RECOMMAND="update mungeno_diary set drecommand=drecommand+1 where did=?";
	private final String DIARY_UPDATE_RPLUS="update mungeno_diary set rcount=rcount+1 where did=?";
	private final String DIARY_UPDATE_RMINUS="update mungeno_diary set rcount=rcount-1 where did=?";
	private final String DIARY_DELETE="delete from mungeno_diary where did=?";
	private final String REPLY_SELECTALL="select * from mungeno_reply where did=?";
	private final String REPLY_INSERT="insert into mungeno_reply(rid,mid,did,rwriter,rcontent) values ((select nvl(max(rid),1000)+1 from mungeno_reply),?,?,?,?)";
	private final String REPLY_UPDATE="update mungeno_reply set rrecommand=rrecommand+1 where rid=?";
	private final String REPLY_DELETE="delete from mungeno_reply where rid=?";
	private final String MEMBER_POINTUP="update mungeno_member set mpoint=mpoint+? where mid like '%'||?||'%'";
	private final String MEMBER_POINTDOWN="update mungeno_member set mpoint=mpoint-? where mid like '%'||?||'%'";
	
	// ????????????
	public void insertDiary(DiaryVO vo) {
		conn=JDBCUtil.connect();
		try {
			conn.setAutoCommit(false); // ????????????

			// ????????????
			pstmt=conn.prepareStatement(DIARY_INSERT);
			pstmt.setString(1, vo.getMid());
			pstmt.setString(2, vo.getDtitle());
			pstmt.setString(3, vo.getDwriter());
			pstmt.setString(4, vo.getDimg());
			pstmt.setString(5, vo.getDcontent());
			pstmt.setString(6, vo.getMregion());
			pstmt.executeUpdate();
			// ???????????????
			int mpoint=10; // ????????? ?????????
			pstmt=conn.prepareStatement(MEMBER_POINTUP);
			pstmt.setInt(1, mpoint);
			pstmt.setString(2, vo.getMid());
			pstmt.executeUpdate();

			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
	}

	// ????????????
	public DiaryVO getDiary(DiaryVO vo) {
		DiaryVO data=null;
		conn=JDBCUtil.connect();
		try {
			pstmt=conn.prepareStatement(DIARY_SELECTONE);
			pstmt.setInt(1, vo.getDid());
			rs=pstmt.executeQuery();

			// ?????? ???????????? ??????
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			if(rs.next()) {
				data=new DiaryVO();
				data.setDid(rs.getInt("did"));
				data.setMid(rs.getString("mid"));
				data.setDtitle(rs.getString("dtitle"));
				data.setDwriter(rs.getString("dwriter"));
				data.setDimg(rs.getString("dimg"));
				data.setDcontent(rs.getString("dcontent"));
				data.setTransdate(sdf.format(rs.getDate("ddate")));
				data.setRcount(rs.getInt("rcount"));
				data.setDrecommand(rs.getInt("drecommand"));
				data.setMregion(rs.getString("mregion"));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
		return data;
	}
	
	// ????????????&??????
	public ArrayList<DiaryVO> getDiaryList(DiaryVO vo,int listCnt){
		ArrayList<DiaryVO> datas=new ArrayList<DiaryVO>();
		String condition=""; // ????????????
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // ????????????
		conn=JDBCUtil.connect();
		
		System.out.println("???????????? : "+vo.getSearchCondition());
		try {
			// ????????? ??????
			if(vo.getSearchCondition().equals("dwriter")) {
				condition="dwriter";
			}
			// ?????? ??????
			else if(vo.getSearchCondition().equals("mregion")) {
				condition="mregion";
			}
			// ?????? ??????
			else if(vo.getSearchCondition().equals("dtitle")) {
				condition="dtitle";
			}
			
			pstmt=conn.prepareStatement(DIARY_SELECTALL_F+condition+DIARY_SELECTALL_B);
			pstmt.setString(1,vo.getSearchKeyword());
			
			rs=pstmt.executeQuery();
			while(rs.next()) {
				DiaryVO data=new DiaryVO(); 
				data = new DiaryVO();
				data.setDid(rs.getInt("did"));
				data.setDtitle(rs.getString("dtitle"));
				data.setDwriter(rs.getString("dwriter"));
				data.setTransdate(sdf.format(rs.getDate("ddate")));
				data.setRcount(rs.getInt("rcount"));
				data.setDrecommand(rs.getInt("drecommand"));
				data.setMregion(rs.getString("mregion"));
				datas.add(data);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
		return datas;
	}

	// ????????????
	public void updateDiary(DiaryVO vo) {
		conn=JDBCUtil.connect();
		try {
			pstmt=conn.prepareStatement(DIARY_UPDATE);
			pstmt.setString(1, vo.getDtitle());
			pstmt.setString(2, vo.getDcontent());
			pstmt.setInt(3, vo.getDid());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
	}
	
	// ????????????
	public void recommandDiary(DiaryVO vo) {
		conn=JDBCUtil.connect();
		try {
			conn.setAutoCommit(false); // ????????????
			
			// ?????? ??????
			pstmt=conn.prepareStatement(DIARY_UPDATE_RECOMMAND);
			pstmt.setInt(1, vo.getDid());
			pstmt.executeUpdate();
			// ????????? ????????? ??????
			int mpoint=2; // ????????? ?????????
			pstmt=conn.prepareStatement(MEMBER_POINTUP);
			pstmt.setInt(1, mpoint);
			pstmt.setString(2, vo.getMid());
			pstmt.executeUpdate();
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
	}

	// ????????????
	public void deleteDiary(DiaryVO vo) {
		conn=JDBCUtil.connect();
		try {
			conn.setAutoCommit(false); // ????????????

			// ????????????
			pstmt=conn.prepareStatement(DIARY_DELETE);
			pstmt.setInt(1, vo.getDid());
			pstmt.executeUpdate();
			// ????????? ??????
			int point=10; // ????????? ?????????
			pstmt=conn.prepareStatement(MEMBER_POINTDOWN);
			pstmt.setInt(1, point);
			pstmt.setString(2, vo.getMid());
			pstmt.executeUpdate();

			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
	}

	// ????????????
	public void insertReply(ReplyVO vo) {
		conn=JDBCUtil.connect();
		try {
			conn.setAutoCommit(false); // ????????????
			// ????????????
			System.out.println("mid: "+vo.getMid());
			pstmt=conn.prepareStatement(REPLY_INSERT);
			pstmt.setString(1, vo.getMid());
			pstmt.setInt(2, vo.getDid());
			pstmt.setString(3, vo.getRwriter());
			pstmt.setString(4, vo.getRcontent());
			pstmt.executeUpdate();
			// ?????? ???????????????
			pstmt=conn.prepareStatement(DIARY_UPDATE_RPLUS);
			pstmt.setInt(1, vo.getDid());
			pstmt.executeUpdate();
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JDBCUtil.disconnect(pstmt, conn);
	}
	
	// ?????? ??????
	public ArrayList<ReplyVO> getReplyList(ReplyVO vo){
		ArrayList<ReplyVO> rdatas=new ArrayList<ReplyVO>();
		conn=JDBCUtil.connect();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // ????????????
		try {
			pstmt=conn.prepareStatement(REPLY_SELECTALL);
			pstmt.setInt(1, vo.getDid());
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()) {
				ReplyVO data=new ReplyVO();
				data.setRid(rs.getInt("rid"));
				data.setMid(rs.getString("mid"));
				data.setRwriter(rs.getString("rwriter"));
				data.setDid(rs.getInt("did"));
				data.setRcontent(rs.getString("rcontent"));
				data.setTransdate(sdf.format(rs.getDate("rdate")));
				data.setRrecommand(rs.getInt("rrecommand"));
				rdatas.add(data);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
		return rdatas;
	}

	// ????????????
	public void updateReply(ReplyVO vo) {
		conn=JDBCUtil.connect();
		try {
			conn.setAutoCommit(false); // ????????????
			
			pstmt=conn.prepareStatement(REPLY_UPDATE);
			pstmt.setInt(1, vo.getRid());
			pstmt.executeUpdate();
			
			// ?????? ????????? ????????? ??????
			int mpoint=1; // ????????? ?????????
			pstmt=conn.prepareStatement(MEMBER_POINTUP);
			pstmt.setInt(1, mpoint);
			pstmt.setString(2, vo.getMid());
			pstmt.executeUpdate();

			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.disconnect(pstmt, conn);
		}
	}

	// ????????????
	public void deleteReply(ReplyVO vo) {
		conn=JDBCUtil.connect();
		try {
			conn.setAutoCommit(false); // ????????????
			// ????????????
			pstmt=conn.prepareStatement(REPLY_DELETE);
			pstmt.setInt(1, vo.getRid());
			pstmt.executeUpdate();
			// ?????? ???????????????
			pstmt=conn.prepareStatement(DIARY_UPDATE_RMINUS);
			pstmt.setInt(1, vo.getDid());
			pstmt.executeUpdate();

			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JDBCUtil.disconnect(pstmt, conn);
	}
}