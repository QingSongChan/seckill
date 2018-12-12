package org.seckill.controller;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.Date;
import java.util.List;


/*
 *@author:PONI_CHAN
 *@date:2018/11/17 15:37
 */
@Controller  //@Service @Component
@RequestMapping("/seckill")
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    /**
     * 获取列表页
     *
     * @return java.lang.String
     * @author chenmc
     * @date 2018/11/17 16:27:00
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("userList", list);
        return "list";
    }

    @GetMapping("modelList")
    public ModelAndView list() {
        List<Seckill> list = seckillService.getSeckillList();
        return new ModelAndView("list", "userList", list);
    }

    /**
     * 获取详情页
     *
     * @return java.lang.String
     * @author chenmc
     * @date 2018/11/17 16:27:30
     */
    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    /**
     * 暴露秒杀地址的结果
     *
     * @return org.seckill.dto.SeckillResult<org.seckill.dto.Exposer>
     * @author chenmc
     * @date 2018/11/17 16:28:32
     */
    @RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody  //springmvc看到这个标签会把返回类型封装成json
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    /**
     * 执行秒杀操作的结果
     *
     * @return org.seckill.dto.SeckillResult<org.seckill.dto.SeckillExecution>
     * @author chenmc
     * @date 2018/11/17 16:29:05
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @CookieValue(value = "killphone", required = false) Long killphone,
                                                   @PathVariable("md5") String md5) {

        if (killphone == null) {
            System.out.println(333);
            System.out.println(killphone);
            return new SeckillResult<SeckillExecution>(false, "手机未注册");
        }

        SeckillResult<SeckillExecution> result;
        //controller就是根据service 的result 对DTO直接封装出结果
        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, killphone, md5);
            System.out.println(3);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (RepeatKillException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
            System.out.println(4);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillCloseException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
            System.out.println(5);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.out.println(6);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        }
    }

    /**
     * 返回当前时间的结果
     *
     * @return
     */
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }
}
