package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.VisitStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客统计聚合 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface VisitStatsMapper extends BaseMapper<VisitStats> {
}
