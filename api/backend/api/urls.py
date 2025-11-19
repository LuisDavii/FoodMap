from django.urls import path
from . import views

urlpatterns = [
    path('api/usuarios/', views.cadastrar_usuario, name='cadastrar_usuario'),
    path('api/login/', views.login_usuario, name='login_usuario'),
    path('api/refeicoes/', views.refeicoes_dispatch, name='refeicoes_dispatch'),
    path('api/refeicoes/<int:refeicao_id>/', views.atualizar_status_refeicao, name='atualizar_status_refeicao'),
]