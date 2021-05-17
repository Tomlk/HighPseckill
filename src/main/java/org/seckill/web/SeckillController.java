package org.seckill.web;

import org.seckill.dao.SeckillResut;
import org.seckill.dto.Exposer;
import org.seckill.entity.Seckill;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/seckill") //url:模块/资源/{id}/细分
public class SeckillController {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(name = "/list",method = RequestMethod.GET)
    public String list(Model model){
        //list.jsp +model=ModelAndView

        List<Seckill> list = seckillService.getSeckillList();

        model.addAttribute("list",list);

        return "list"; //"/WEB-INF/jsp/"list".jsp
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") long seckillId, Model model)
    {
        if(Objects.isNull(seckillId))
            return "redirect:/seckill/list";

        Seckill seckill = seckillService.getById(seckillId);

        if(Objects.isNull(seckill))
            return "forward:/seckill/list";

        model.addAttribute("seckill",seckill);
        return "detail";
    }

    //ajax json
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"}) //直接浏览器敲无效
    @ResponseBody //json类型
    public SeckillResut<Exposer> exposer(long seckillId)
    {
        SeckillResut<Exposer> result;

        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result=new SeckillResut<Exposer>(true,exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            result=new SeckillResut<Exposer>(false,e.getMessage());
        }
        return result;


    }
}
