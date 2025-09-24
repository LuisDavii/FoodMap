from django.urls import path
from . import views

urlpatterns = [
    path('api/usuarios/', views.cadastrar_usuario, name='cadastrar_usuario'),
    path('api/login/', views.login_usuario, name='login_usuario'),
]