/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2017-04-08 01:54 创建
 */
package demo.flow.transfer.processor;

import demo.entity.ModifyAccount;
import demo.entity.Transfer;
import demo.enums.Direction;
import demo.enums.ModifyAccountStatus;
import demo.enums.ModifyAccountType;
import demo.enums.ResultStatus;
import demo.utils.OID;
import org.bekit.flow.FlowEngine;
import org.bekit.flow.annotation.processor.*;
import org.bekit.flow.engine.TargetContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

/**
 * 恢复付款人资金处理器
 */
@Processor
public class RestorePayerProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RestorePayerProcessor.class);

    @Autowired
    private FlowEngine flowEngine;

    @ProcessorBefore
    public void before(TargetContext<Transfer> targetContext) {
        logger.info("执行RestorePayerProcessor.before");
    }

    @ProcessorExecute
    public ResultStatus execute(TargetContext<Transfer> targetContext) throws TimeoutException {
        logger.info("执行RestorePayerProcessor.execute");

        ModifyAccount modifyAccount = buildModifyAccount(targetContext.getTarget());
        modifyAccount = flowEngine.insertTargetAndStart("modifyAccountFlow", modifyAccount, null);
        switch (modifyAccount.getStatus()) {
            case SUCCESS:
                return ResultStatus.SUCCESS;
            case FAIL:
                return ResultStatus.FAIL;
            default:
                return ResultStatus.PROCESS;
        }
    }

    private ModifyAccount buildModifyAccount(Transfer transfer) {
        ModifyAccount modifyAccount = new ModifyAccount();
        modifyAccount.setTransferBizNo(transfer.getBizNo());
        modifyAccount.setTransferStatus(transfer.getStatus());
        modifyAccount.setType(ModifyAccountType.FORCE_SUCCESS);     // 必须成功
        modifyAccount.setAccountNo(transfer.getPayerAccountNo());
        modifyAccount.setDirection(Direction.UP);
        modifyAccount.setAmount(transfer.getAmount());
        modifyAccount.setStatus(ModifyAccountStatus.MODIFY);
        modifyAccount.setRefOrderNo(OID.newId());

        return modifyAccount;
    }

    @ProcessorAfter
    public void after(TargetContext<Transfer> targetContext) {
        logger.info("执行RestorePayerProcessor.after");
    }

    @ProcessorEnd
    public void end(TargetContext<Transfer> targetContext) {
        logger.info("执行RestorePayerProcessor.end");
    }

    @ProcessorError
    public void error(TargetContext<Transfer> targetContext) {
        logger.error("执行RestorePayerProcessor.error");
    }
}
