from utils import set_state_task
from utils import operation_task
from utils import link_tasks
from utils import CustomContext
from utils import generate_native_node_workflows

# content of this fn can be generated by workflow plugin (see workflows.py in generated blueprint)
def _build_tasks(ctx, graph, custom_context):
    # just paste here the generated code
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'starting', 'PHP_starting', custom_context)
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'started', 'Wordpress_started', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'configuring', 'Mysql_configuring', custom_context)
    operation_task(ctx, graph, 'Mysql', 'cloudify.interfaces.lifecycle.create', 'create_Mysql', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'initial', 'Apache_initial', custom_context)
    operation_task(ctx, graph, 'Mysql', 'cloudify.interfaces.lifecycle.configure', 'configure_Mysql', custom_context)
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'configured', 'Wordpress_configured', custom_context)
    custom_context.register_native_delegate_wf_step('Compute', 'Compute_install')
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'starting', 'Wordpress_starting', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'created', 'Mysql_created', custom_context)
    operation_task(ctx, graph, 'PHP', 'cloudify.interfaces.lifecycle.start', 'start_PHP', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'starting', 'Apache_starting', custom_context)
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'configuring', 'PHP_configuring', custom_context)
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'created', 'PHP_created', custom_context)
    operation_task(ctx, graph, 'Wordpress', 'cloudify.interfaces.lifecycle.start', 'start_Wordpress', custom_context)
    operation_task(ctx, graph, 'Apache', 'cloudify.interfaces.lifecycle.configure', 'configure_Apache', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'creating', 'Mysql_creating', custom_context)
    operation_task(ctx, graph, 'Apache', 'cloudify.interfaces.lifecycle.create', 'create_Apache', custom_context)
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'initial', 'Wordpress_initial', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'configured', 'Apache_configured', custom_context)
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'started', 'PHP_started', custom_context)
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'configuring', 'Wordpress_configuring', custom_context)
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'creating', 'PHP_creating', custom_context)
    operation_task(ctx, graph, 'Wordpress', 'cloudify.interfaces.lifecycle.configure', 'configure_Wordpress', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'starting', 'Mysql_starting', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'configured', 'Mysql_configured', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'creating', 'Apache_creating', custom_context)
    operation_task(ctx, graph, 'PHP', 'cloudify.interfaces.lifecycle.create', 'create_PHP', custom_context)
    operation_task(ctx, graph, 'Apache', 'cloudify.interfaces.lifecycle.start', 'start_Apache', custom_context)
    operation_task(ctx, graph, 'PHP', 'cloudify.interfaces.lifecycle.configure', 'configure_PHP', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'started', 'Mysql_started', custom_context)
    custom_context.register_native_delegate_wf_step('Network', 'Network_install')
    operation_task(ctx, graph, 'Wordpress', 'cloudify.interfaces.lifecycle.create', 'create_Wordpress', custom_context)
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'creating', 'Wordpress_creating', custom_context)
    custom_context.add_customized_wf_node('Mysql')
    set_state_task(ctx, graph, 'Mysql', 'initial', 'Mysql_initial', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'created', 'Apache_created', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'started', 'Apache_started', custom_context)
    custom_context.add_customized_wf_node('Wordpress')
    set_state_task(ctx, graph, 'Wordpress', 'created', 'Wordpress_created', custom_context)
    custom_context.add_customized_wf_node('Apache')
    set_state_task(ctx, graph, 'Apache', 'configuring', 'Apache_configuring', custom_context)
    operation_task(ctx, graph, 'Mysql', 'cloudify.interfaces.lifecycle.start', 'start_Mysql', custom_context)
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'initial', 'PHP_initial', custom_context)
    custom_context.add_customized_wf_node('PHP')
    set_state_task(ctx, graph, 'PHP', 'configured', 'PHP_configured', custom_context)
    custom_context.register_native_delegate_wf_step('Compute2', 'Compute2_install')
    custom_context.register_native_delegate_wf_step('DeletableConfigurableBlockStorage', 'DeletableConfigurableBlockStorage_install')
    generate_native_node_workflows(ctx, graph, custom_context, 'install')
    link_tasks(graph, 'PHP_starting', 'PHP_configured', custom_context)
    link_tasks(graph, 'Wordpress_started', 'start_Wordpress', custom_context)
    link_tasks(graph, 'Mysql_configuring', 'Wordpress_created', custom_context)
    link_tasks(graph, 'Mysql_configuring', 'Mysql_created', custom_context)
    link_tasks(graph, 'create_Mysql', 'Mysql_creating', custom_context)
    link_tasks(graph, 'Apache_initial', 'Compute2_install', custom_context)
    link_tasks(graph, 'configure_Mysql', 'Mysql_configuring', custom_context)
    link_tasks(graph, 'Wordpress_configured', 'configure_Wordpress', custom_context)
    link_tasks(graph, 'Wordpress_starting', 'Wordpress_configured', custom_context)
    link_tasks(graph, 'Mysql_created', 'create_Mysql', custom_context)
    link_tasks(graph, 'start_PHP', 'PHP_starting', custom_context)
    link_tasks(graph, 'Apache_starting', 'Apache_configured', custom_context)
    link_tasks(graph, 'PHP_configuring', 'PHP_created', custom_context)
    link_tasks(graph, 'PHP_configuring', 'Wordpress_created', custom_context)
    link_tasks(graph, 'PHP_created', 'create_PHP', custom_context)
    link_tasks(graph, 'start_Wordpress', 'Wordpress_starting', custom_context)
    link_tasks(graph, 'configure_Apache', 'Apache_configuring', custom_context)
    link_tasks(graph, 'Mysql_creating', 'Mysql_initial', custom_context)
    link_tasks(graph, 'create_Apache', 'Apache_creating', custom_context)
    link_tasks(graph, 'Wordpress_initial', 'Apache_started', custom_context)
    link_tasks(graph, 'Apache_configured', 'configure_Apache', custom_context)
    link_tasks(graph, 'PHP_started', 'start_PHP', custom_context)
    link_tasks(graph, 'Wordpress_configuring', 'Mysql_started', custom_context)
    link_tasks(graph, 'Wordpress_configuring', 'PHP_started', custom_context)
    link_tasks(graph, 'Wordpress_configuring', 'Wordpress_created', custom_context)
    link_tasks(graph, 'PHP_creating', 'PHP_initial', custom_context)
    link_tasks(graph, 'configure_Wordpress', 'Wordpress_configuring', custom_context)
    link_tasks(graph, 'Mysql_starting', 'Mysql_configured', custom_context)
    link_tasks(graph, 'Mysql_configured', 'configure_Mysql', custom_context)
    link_tasks(graph, 'Apache_creating', 'Apache_initial', custom_context)
    link_tasks(graph, 'create_PHP', 'PHP_creating', custom_context)
    link_tasks(graph, 'start_Apache', 'Apache_starting', custom_context)
    link_tasks(graph, 'configure_PHP', 'PHP_configuring', custom_context)
    link_tasks(graph, 'Mysql_started', 'start_Mysql', custom_context)
    link_tasks(graph, 'create_Wordpress', 'Wordpress_creating', custom_context)
    link_tasks(graph, 'Wordpress_creating', 'Wordpress_initial', custom_context)
    link_tasks(graph, 'Mysql_initial', 'Compute_install', custom_context)
    link_tasks(graph, 'Apache_created', 'create_Apache', custom_context)
    link_tasks(graph, 'Apache_started', 'start_Apache', custom_context)
    link_tasks(graph, 'Wordpress_created', 'create_Wordpress', custom_context)
    link_tasks(graph, 'Apache_configuring', 'Apache_created', custom_context)
    link_tasks(graph, 'start_Mysql', 'Mysql_starting', custom_context)
    link_tasks(graph, 'PHP_initial', 'Compute2_install', custom_context)
    link_tasks(graph, 'PHP_configured', 'configure_PHP', custom_context)



def build_tasks(ctx, graph):
    custom_context = CustomContext(ctx)
    _build_tasks(ctx, graph, custom_context)
    return custom_context