package org.seckill.web;

import org.seckill.dao.SeckillResut;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/seckill") //url:模块/资源/{id}/细分
public class SeckillController {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){
        //list.jsp +model=ModelAndView

        System.out.println("你好");
        List<Seckill> list = seckillService.getSeckillList();

        model.addAttribute("list",list);

        return "list"; //"/WEB-INF/jsp/"list".jsp
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") long seckillId, Model model) {
        if(Objects.isNull(seckillId))
            return "redirect:/seckill/list";

        Seckill seckill = seckillService.getById(seckillId);

        if(Objects.isNull(seckill))
            return "forward:/seckill/list";

        model.addAttribute("seckill",seckill);
        System.out.println("进入detail");
        return "detail";
    }

    //ajax json
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"}) //直接浏览器敲无效
    @ResponseBody //json类型
    public SeckillResut<Exposer> exposer(@PathVariable("seckillId")  Long seckillId) {
        SeckillResut<Exposer> result;
        System.out.println("进入exposer");
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);//秒杀已经开启
            result=new SeckillResut<Exposer>(true,exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            result=new SeckillResut<Exposer>(false,e.getMessage());
        }
        return result;

    }

    @RequestMapping(value="/{seckillId}/{md5}/execution",
    method =RequestMethod.POST,
    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResut<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                  @PathVariable("md5") String md5,
                                                  @CookieValue(value = "killPhone",required = false) Long phone){
        System.out.println("进入execution");
        if(phone==null){
            return new SeckillResut<SeckillExecution>(false,"未注册");
        }
        SeckillResut<SeckillResut> result;

        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResut<SeckillExecution>(true,execution);
        }catch (RepeatKillException e){
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResut<SeckillExecution>(true,execution);
        }catch (SeckillCloseException e){
            SeckillExecution execution=new SeckillExecution(seckillId,SeckillStatEnum.END);
            return new SeckillResut<SeckillExecution>(true,execution);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            //TODO
            SeckillExecution execution=new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
            return new SeckillResut<SeckillExecution>(true,execution);
        }

    }

    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResut<Long> time(){
        System.out.println("进入time");
        Date now = new Date();
        return new SeckillResut(true,now.getTime());
    }
}
