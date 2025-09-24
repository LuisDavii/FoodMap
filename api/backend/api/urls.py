from django.urls import path
from . import views

urlpatterns = [
    path('api/usuarios/', views.cadastrar_usuario, name='cadastrar_usuario'),
]